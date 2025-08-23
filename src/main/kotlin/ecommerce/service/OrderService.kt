package ecommerce.service

import ecommerce.dto.CreateOrderResponse
import ecommerce.dto.MemberResponse
import ecommerce.entity.Order
import ecommerce.entity.OrderItem
import ecommerce.entity.Payment
import ecommerce.enums.OrderAndPaymentStatus
import ecommerce.handler.OrderCreationException
import ecommerce.handler.PaymentFailedException
import ecommerce.repository.CartItemRepositoryJpa
import ecommerce.repository.CartRepositoryJpa
import ecommerce.repository.MemberRepositoryJpa
import ecommerce.repository.OrderItemRepository
import ecommerce.repository.OrderRepository
import ecommerce.repository.PaymentRepository
import ecommerce.service.payment.StripeClientService
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val cartRepository: CartRepositoryJpa,
    private val cartItemRepository: CartItemRepositoryJpa,
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val paymentRepository: PaymentRepository,
    private val memberRepository: MemberRepositoryJpa,
    private val stripeClientService: StripeClientService,
    private val optionService: OptionService,
    private val applicationContext: ApplicationContext,
) {
    // Get proxy instance for transactional methods
    private val self: OrderService
        get() = applicationContext.getBean(OrderService::class.java)

    @Transactional
    fun createOrder(
        memberId: Long,
        productOptionId: Long,
        quantity: Int,
        paymentMethod: String,
        currency: String,
    ): CreateOrderResponse {
        val cart =
            cartRepository.findByMemberId(memberId)
                ?: throw NoSuchElementException("Cart not found")

        val cartItem =
            cartItemRepository.findByCartIdAndProductOptionId(cart.id!!, productOptionId)
                ?: throw NoSuchElementException("Cart item not found")

        val product = cartItem.product

        val member =
            memberRepository.findById(memberId)
                .orElseThrow { NoSuchElementException("Member not found") }

        val totalPrice = product.price * quantity.toDouble()
        val amountInCents = (totalPrice * 100).toLong() // Stripe requires cents

        val order =
            try {
                orderRepository.save(
                    Order(
                        member = member,
                        status = OrderAndPaymentStatus.PENDING,
                    ),
                )
            } catch (e: Exception) {
                throw OrderCreationException("Order creation failed: ${e.message}")
            }

        try {
            orderItemRepository.save(
                OrderItem(
                    order = order,
                    product = product,
                    productOption = cartItem.productOption,
                    quantity = quantity,
                ),
            )
        } catch (e: Exception) {
            throw OrderCreationException("Order item creation failed: ${e.message}")
        }

        val paymentIntentId =
            try {
                stripeClientService.createPaymentIntent(
                    amountInCents,
                    currency,
                    paymentMethod,
                )
            } catch (e: Exception) {
                throw PaymentFailedException("Payment failed: ${e.message}")
            }

        try {
            paymentRepository.save(
                Payment(
                    order = order,
                    status = OrderAndPaymentStatus.PENDING,
                    stripePaymentIntentId = paymentIntentId,
                    amount = amountInCents,
                ),
            )
        } catch (e: Exception) {
            throw PaymentFailedException("Payment persistence failed: ${e.message}")
        }

        cartRepository.save(cart)

        return CreateOrderResponse(
            orderId = order.id!!,
            paymentIntentId = paymentIntentId,
        )
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

    @Transactional
    fun preparePaymentProcessing(
        orderId: Long,
        member: MemberResponse,
    ): String {
        val order =
            orderRepository.findByIdAndMemberId(orderId, member.id)
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

    // This method handles the external call and final confirmation (NOT transactional)
    fun confirmPayment(
        orderId: Long,
        member: MemberResponse,
    ): String {
        // self call transactional methods through proxy
        val stripePaymentIntentId = self.preparePaymentProcessing(orderId, member)

        return try {
            stripeClientService.confirmPaymentIntent(stripePaymentIntentId)
            self.completeSuccessfulPayment(stripePaymentIntentId)
            "Payment successful, order $orderId marked as PAID"
        } catch (e: Exception) {
            self.markPaymentAsFailed(stripePaymentIntentId, e.message ?: "Unknown error")
            throw PaymentFailedException("Payment processing failed: ${e.message}")
        }
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

        // Update inventory - use safe calls and null checks
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
}
