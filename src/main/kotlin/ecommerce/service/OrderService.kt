package ecommerce.service

import ecommerce.dto.order.CreateOrderItemRequest
import ecommerce.dto.order.CreateOrderRequest
import ecommerce.dto.order.OrderResponse
import ecommerce.dto.order.toResponse
import ecommerce.exception.NotFoundException
import ecommerce.model.Order
import ecommerce.model.OrderItem
import ecommerce.model.OrderStatus
import ecommerce.model.PaymentStatus
import ecommerce.repository.CartItemRepository
import ecommerce.repository.CartRepository
import ecommerce.repository.MemberRepository
import ecommerce.repository.OrderRepository
import ecommerce.repository.ProductOptionRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val memberRepository: MemberRepository,
    private val productOptionRepository: ProductOptionRepository,
    private val cartItemRepository: CartItemRepository,
    private val cartRepository: CartRepository,
) {
    @Transactional
    fun createOrder(
        request: CreateOrderRequest,
        memberId: Long?,
    ): Order {
        requireNotNull(memberId) { "Member ID cannot be null" }

        val member =
            memberRepository.findById(memberId)
                .orElseThrow { NotFoundException("Member not found with id: $memberId") }

        validateOrderRequest(request, memberId)

        val order =
            Order(
                member = member,
                currency = request.currency,
                orderStatus = OrderStatus.PENDING,
                paymentStatus = PaymentStatus.PENDING,
            )

        request.cartItemIds.forEach { cartItemId ->
            val cartItem =
                cartItemRepository.findById(cartItemId)
                    .orElseThrow {
                        NotFoundException("Cart item not found with id: $cartItemId")
                    }

            val orderItem = createOrderItemFromCart(cartItem, order)
            order.addOrderItem(orderItem)
        }

        order.calculateTotalAmount()

        return orderRepository.save(order)
    }

    fun getById(id: Long): Order {
        return orderRepository.findById(id)
            .orElseThrow { NotFoundException("Order not found with id: $id") }
    }

    @Transactional
    fun save(order: Order): Order {
        return orderRepository.save(order)
    }

    fun getOrdersByMember(
        memberId: Long,
        pageable: Pageable,
    ): Page<OrderResponse> {
        return orderRepository.findByMemberId(memberId, pageable)
            .map { it.toResponse() }
    }

    fun findAllOrders(pageable: Pageable): Page<OrderResponse> {
        return orderRepository.findAll(pageable)
            .map { it.toResponse() }
    }

    @Transactional
    fun deleteById(orderId: Long) {
        val order =
            orderRepository.findById(orderId)
                .orElseThrow { NotFoundException("Order not found with id: $orderId") }

        if (order.orderStatus == OrderStatus.CONFIRMED || order.paymentStatus == PaymentStatus.COMPLETED) {
            throw IllegalStateException("Cannot delete confirmed or completed orders")
        }

        orderRepository.deleteById(orderId)
    }

    @Transactional
    fun confirmOrderPayment(orderId: Long): Order {
        val order =
            orderRepository.findById(orderId)
                .orElseThrow { NotFoundException("Order not found with id: $orderId") }

        if (order.paymentStatus == PaymentStatus.COMPLETED) {
            throw IllegalStateException("Payment already confirmed")
        }

        order.orderStatus = OrderStatus.CONFIRMED
        order.paymentStatus = PaymentStatus.COMPLETED

        updateProductStock(order)
        clearCartItems(order)

        return orderRepository.save(order)
    }

    private fun validateOrderRequest(
        request: CreateOrderRequest,
        memberId: Long,
    ) {
        if (request.cartItemIds.isEmpty()) {
            throw IllegalArgumentException("Order must contain at least one cart item")
        }

        request.cartItemIds.forEach { cartItemId ->
            val cartItem =
                cartItemRepository.findById(cartItemId)
                    .orElseThrow { NotFoundException("Cart item not found with id: $cartItemId") }

            val cartMemberId = cartItem.cart.member?.id
            if (cartMemberId != memberId) {
                throw IllegalArgumentException("Cart item $cartItemId does not belong to member $memberId")
            }

            if (cartItem.productOption.quantity < cartItem.quantity) {
                throw IllegalArgumentException(
                    "Insufficient stock for ${cartItem.productOption.name}. " +
                        "Available: ${cartItem.productOption.quantity}, " +
                        "Requested: ${cartItem.quantity}",
                )
            }
        }
    }

    private fun createOrderItem(
        request: CreateOrderItemRequest,
        order: Order,
    ): OrderItem {
        val productOption =
            productOptionRepository.findById(request.productOptionId)
                .orElseThrow { NotFoundException("Product option not found. id:${request.productOptionId}") }

        return OrderItem.fromProductOption(productOption, request.quantity, order)
    }

    private fun createOrderItemFromCart(
        cartItem: ecommerce.model.CartItem,
        order: Order,
    ): OrderItem {
        return OrderItem.fromProductOption(cartItem.productOption, cartItem.quantity, order)
    }

    private fun updateProductStock(order: Order) {
        order.orderItems.forEach { orderItem ->
            val productOption = orderItem.productOption
            productOption.subtract(orderItem.quantity)
            productOptionRepository.save(productOption)
        }
    }

    private fun clearCartItems(order: Order) {
        val member = order.member
        val cart = cartRepository.findByMemberId(member.id ?: 0L)

        if (cart != null) {
            order.orderItems.forEach { orderItem ->
                val cartItems =
                    cartItemRepository.findByCartIdAndProductOptionId(
                        cart.id ?: 0L,
                        orderItem.productOption.id ?: 0L,
                    )
                cartItems.forEach { cartItem ->
                    if (cartItem.quantity <= orderItem.quantity) {
                        cartItemRepository.delete(cartItem)
                    } else {
                        cartItem.quantity -= orderItem.quantity
                        cartItemRepository.save(cartItem)
                    }
                }
            }
        }
    }
}
