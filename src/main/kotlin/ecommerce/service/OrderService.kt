package ecommerce.service

import ecommerce.dto.OrderPlaceForm
import ecommerce.dto.OrderResponse
import ecommerce.exception.EmptyCartException
import ecommerce.exception.InternalServerErrorException
import ecommerce.exception.NotFoundException
import ecommerce.exception.PaymentFailedException
import ecommerce.model.Order
import ecommerce.model.OrderItem
import ecommerce.model.OrderStatus
import ecommerce.repository.CartItemRepository
import ecommerce.repository.MemberRepository
import ecommerce.repository.OptionRepository
import ecommerce.repository.OrderItemRepository
import ecommerce.repository.OrderRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class OrderService(
    private val memberRepository: MemberRepository,
    private val cartItemRepository: CartItemRepository,
    private val optionRepository: OptionRepository,
    private val orderItemRepository: OrderItemRepository,
    private val orderRepository: OrderRepository,
    private val orderPaymentService: OrderPaymentService,
) {
    fun readOrders(memberId: Long): List<OrderResponse> {
        val member =
            memberRepository.findByIdOrNull(memberId)
                ?: throw NotFoundException(MESSAGE_ORDER_NOT_FOUND)
        return member.orders.map { Order.to(it) }
    }

    fun readOrder(
        orderId: Long,
        memberId: Long,
    ): OrderResponse {
        memberRepository.findByIdOrNull(memberId)
            ?: throw NotFoundException(MESSAGE_ORDER_NOT_FOUND)
        val order =
            orderRepository.findByIdOrNull(orderId)
                ?: throw NotFoundException(MESSAGE_ORDER_NOT_FOUND)
        val savedOrder = orderRepository.save(order)
        val orderResponse = Order.to(savedOrder)
        return orderResponse
    }

    fun placeOrder(
        memberId: Long,
        orderForm: OrderPlaceForm,
    ): Order {
        if (orderForm.cartItemIds.isEmpty()) throw EmptyCartException(MESSAGE_EMPTY_CART)
        val member =
            memberRepository.findByIdOrNull(memberId)
                ?: throw NotFoundException(MESSAGE_ORDER_NOT_FOUND)
        val cartItems = cartItemRepository.findAllById(orderForm.cartItemIds)
        val orderItems = cartItems.map { OrderItem.from(it) }
        orderItems.forEach { it.option.subtract(it.quantity) }
        val options = orderItems.map { it.option }
        val order =
            Order(
                member = member,
                orderItems = orderItems,
                currency = orderForm.currency,
                paymentMethod = orderForm.paymentMethod,
            )
        order.status = OrderStatus.POST_PAYMENT
        member.addOrder(order)
        val paymentResponse =
            try {
                orderPaymentService.initiatePayment(order)
            } catch (e: IllegalArgumentException) {
                throw PaymentFailedException(e.message ?: "Payment failed")
            }
        order.status = OrderStatus.ORDERED
        order.checkoutSessionId = paymentResponse.id
        cartItemRepository.deleteAll(cartItems)
        optionRepository.saveAll(options)
        orderItemRepository.saveAll(orderItems)
        val savedOrder = orderRepository.save(order)
        memberRepository.save(member)
        return orderRepository.findByIdOrNull(savedOrder.id)
            ?: throw InternalServerErrorException(MESSAGE_ORDER_NOT_FOUND)
    }

    companion object {
        const val MESSAGE_EMPTY_CART = "No cart item provided in cart to order"
        const val MESSAGE_ORDER_NOT_FOUND = "Order not found"
    }
}
