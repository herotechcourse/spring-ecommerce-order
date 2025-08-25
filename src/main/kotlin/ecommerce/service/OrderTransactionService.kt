package ecommerce.service

import ecommerce.entity.Order
import ecommerce.enums.OrderAndPaymentStatus
import ecommerce.handler.PaymentFailedException
import ecommerce.repository.CartRepositoryJpa
import ecommerce.repository.OrderRepository
import ecommerce.repository.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderTransactionService(
    private val cartRepository: CartRepositoryJpa,
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
    private val optionService: OptionService,
) {
    @Transactional
    fun preparePaymentProcessing(
        orderId: Long,
        memberId: Long,
    ): String {
        val order =
            orderRepository.findByIdAndMemberId(orderId, memberId)
                ?: throw NoSuchElementException("Order not found")

        val payment =
            order.payment
                ?: throw NoSuchElementException("Payment not found")

        if (payment.stripePaymentIntentId == null) {
            throw NoSuchElementException("Stripe payment intent not found")
        }

        if (payment.status == OrderAndPaymentStatus.PAID) {
            throw PaymentFailedException("Payment already made, status = ${payment.status}")
        }

        payment.status = OrderAndPaymentStatus.PROCESSING
        paymentRepository.save(payment)
        order.status = OrderAndPaymentStatus.PROCESSING
        orderRepository.save(order)

        return payment.stripePaymentIntentId
    }

    @Transactional
    fun completeSuccessfulPayment(stripePaymentIntentId: String) {
        val payment =
            paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId)
                ?: throw NoSuchElementException("Payment not found")

        payment.status = OrderAndPaymentStatus.PAID
        paymentRepository.save(payment)

        val order = payment.order ?: throw IllegalStateException("Order not found for payment $stripePaymentIntentId")

        order.status = OrderAndPaymentStatus.PAID
        orderRepository.save(order)

        // Update inventory
        order.orderItems.forEach { orderItem ->
            val productOption = orderItem.productOption
            if (productOption != null && productOption.id != null) {
                optionService.decreaseOptionQuantity(productOption.id, orderItem.quantity)
            }
        }

        cleanCartItemsForOrder(order)
    }

    @Transactional
    fun markPaymentAsFailed(
        stripePaymentIntentId: String,
        errorMessage: String,
    ) {
        val payment = paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId)
        payment?.status = OrderAndPaymentStatus.FAILED
        payment?.let { paymentRepository.save(it) }

        val order = payment?.order
        order?.status = OrderAndPaymentStatus.FAILED
        order?.let { orderRepository.save(it) }
    }

    @Transactional
    fun cleanCartItemsForOrder(order: Order) {
        val cart =
            cartRepository.findByMemberId(order.member!!.id!!)
                ?: throw NoSuchElementException("Cart not found")

        val orderItemProductOptions = order.orderItems.mapNotNull { it.productOption?.id }.toSet()
        cart.cartItems.removeIf { it.productOption.id in orderItemProductOptions }

        cartRepository.save(cart)
    }
}
