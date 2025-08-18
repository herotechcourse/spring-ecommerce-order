package ecommerce.service

import com.stripe.model.PaymentIntent
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
    private val cartService: CartService,
    private val optionService: OptionService,
) {
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

        val paymentIntent: PaymentIntent =
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
                    stripePaymentIntentId = paymentIntent.id,
                    amount = amountInCents,
                ),
            )
        } catch (e: Exception) {
            throw PaymentFailedException("Payment persistence failed: ${e.message}")
        }

        cartRepository.save(cart)

        return CreateOrderResponse(
            orderId = order.id!!,
            paymentIntentId = paymentIntent.id,
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
    fun processPayment(
        orderId: Long,
        member: MemberResponse,
    ): String {
        try {
            val order =
                orderRepository.findByIdAndMemberId(orderId, member.id)

            if (order == null) {
                throw NoSuchElementException("Order not found")
            }

            val payment = order.payment

            if (payment == null || payment.stripePaymentIntentId == null) {
                throw NoSuchElementException("Payment not found")
            }

            if (payment.status == OrderAndPaymentStatus.PAID) {
                throw PaymentFailedException("Payment already made, status = ${payment.status}")
            }

            stripeClientService.confirmPaymentIntent(payment.stripePaymentIntentId)

            payment.status = OrderAndPaymentStatus.PAID
            paymentRepository.save(payment)

            order.status = OrderAndPaymentStatus.PAID
            orderRepository.save(order)

            order.orderItems.forEach { orderItem ->
                val productOption = orderItem.productOption ?: return@forEach
                optionService.decreaseOptionQuantity(productOption.id!!, orderItem.quantity)
            }

            cleanCartItemsForOrder(order)

            return "Payment successful, order ${order.id} marked as PAID"
        } catch (e: Exception) {
            throw PaymentFailedException("Payment processing failed: ${e.message}")
        }
    }
}
