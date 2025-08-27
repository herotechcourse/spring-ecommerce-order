package ecommerce.service

import ecommerce.dto.OrderStatusResponse
import ecommerce.dto.stripe.PaymentRequest
import ecommerce.dto.stripe.PaymentResponse
import ecommerce.exception.ForbiddenException
import ecommerce.model.Cart
import ecommerce.model.Member
import ecommerce.model.Order
import ecommerce.model.OrderItem
import ecommerce.model.OrderStatus
import ecommerce.model.Payment
import ecommerce.repository.CartJpaRepository
import ecommerce.repository.MemberJpaRepository
import ecommerce.repository.OrderJpaRepository
import ecommerce.repository.findByIdOrThrow
import ecommerce.repository.getByIdOrThrow
import ecommerce.repository.getByMemberIdAndValidateNotEmpty
import ecommerce.stripe.StripeClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class OrderService(
    private val orderRepository: OrderJpaRepository,
    private val memberRepository: MemberJpaRepository,
    private val cartRepository: CartJpaRepository,
    private val stripeClient: StripeClient,
) {
    @Transactional
    fun placeOrder(
        memberId: Long,
        paymentRequest: PaymentRequest,
    ): Order {
        val member = memberRepository.getByIdOrThrow(memberId)
        val cart = cartRepository.getByMemberIdAndValidateNotEmpty(memberId)

        val order = createOrderFromCart(member, cart)

        val paymentResponse = processStripePayment(order, paymentRequest)

        updateOrderStatusBasedOnPayment(order, paymentResponse, cart)

        return orderRepository.save(order)
    }

    private fun createOrderFromCart(
        member: Member,
        cart: Cart,
    ): Order {
        val initialOrder =
            Order(
                member = member,
                status = OrderStatus.PENDING,
            )
        val orderItems =
            cart.cartProducts.map { cartItem ->
                val product = cartItem.product
                val option = cartItem.option

                if (option.quantity < cartItem.quantity) {
                    throw IllegalStateException("Insufficient stock for product: ${product.name}, option: ${option.name}")
                }
                option.reduceOptionQuantity(cartItem.quantity)

                OrderItem(
                    order = initialOrder,
                    product = product,
                    option = option,
                    quantity = cartItem.quantity,
                    price = product.price,
                )
            }
        initialOrder.items.addAll(orderItems)
        return orderRepository.save(initialOrder)
    }

    private fun processStripePayment(
        order: Order,
        paymentRequest: PaymentRequest,
    ): PaymentResponse {
        val totalAmount = order.items.sumOf { it.price * it.quantity }
        val amountInCents = (totalAmount * 100).toInt()
        val stripeRequest = paymentRequest.copy(amount = amountInCents)

        val paymentResponse = stripeClient.createCheckoutSession(stripeRequest)

        val payment =
            Payment(
                order = order,
                checkoutSessionId = paymentResponse.id,
                paymentIntentId = null,
                amount = totalAmount.toInt(),
                currency = paymentRequest.currency,
                status = paymentResponse.status,
                createdAt = LocalDateTime.now(),
            )
        order.payment = payment
        return paymentResponse
    }

    private fun updateOrderStatusBasedOnPayment(
        order: Order,
        paymentResponse: PaymentResponse,
        cart: Cart,
    ) {
        if (paymentResponse.status == "succeeded") {
            order.status = OrderStatus.COMPLETED
            cart.clearCartProducts()
            cartRepository.save(cart)
        } else {
            order.status = OrderStatus.CANCELLED
        }
    }

    fun getOrderStatus(
        orderId: Long,
        memberId: Long,
    ): OrderStatusResponse {
        val order = orderRepository.findByIdOrThrow(orderId)
        if (order.member.id != memberId) {
            throw ForbiddenException("You are not authorized to view this order's status.")
        }
        return OrderStatusResponse(order.id, order.status)
    }
}
