package ecommerce.service

import ecommerce.dto.OrderRequest
import ecommerce.dto.OrderResponse
import ecommerce.dto.PaymentResponse
import ecommerce.enum.OrderStatus
import ecommerce.enum.PaymentStatus
import ecommerce.exception.CartException
import ecommerce.mapper.toOrderDto
import ecommerce.mapper.toOrderItem
import ecommerce.model.Cart
import ecommerce.model.Order
import ecommerce.repository.CartJpaRepository
import ecommerce.repository.OrderJpaRepository
import ecommerce.repository.getByMemberId
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Transactional
@Service
class OderService(
    private val cartJpaRepository: CartJpaRepository,
    private val orderJpaRepository: OrderJpaRepository,
    private val paymentService: PaymentService,
    private val cartService: CartService,
) {
    fun processOrder(
        memberId: Long,
        request: OrderRequest,
    ): OrderResponse {
        val cart = cartJpaRepository.getByMemberId(memberId)
        if (cart.cartProducts.isEmpty()) {
            throw CartException("Cart is empty")
        }
        cart.cartProducts.forEach { it.option.checkAvailabilityInStock(it.quantity) }
        val amount = cart.totalAmount
        val paymentResponse = paymentService.createPaymentIntent(request, amount)
        val newOrder = placeOrder(cart, memberId, paymentResponse)
        updatePaymentAndOrderStatus(newOrder, paymentResponse, cart)
        return OrderResponse(newOrder.toOrderDto(), paymentResponse)
    }

    private fun updatePaymentAndOrderStatus(
        newOrder: Order,
        paymentResponse: PaymentResponse,
        cart: Cart,
    ) {
        if (paymentResponse.status == PaymentStatus.SUCCESS.name) {
            newOrder.updateStatus(OrderStatus.PAID)
            paymentService.updatePaymentStatus(paymentResponse.id, PaymentStatus.SUCCESS)
            cartService.cartCheckOut(cart)
        } else {
            newOrder.updateStatus(OrderStatus.FAILED)
        }

        orderJpaRepository.save(newOrder)
    }

    private fun placeOrder(
        cart: Cart,
        memberId: Long,
        paymentResponse: PaymentResponse,
    ): Order {
        val amount = BigDecimal.valueOf(paymentResponse.amount.toLong(), 2)
        val orderItems = cart.cartProducts.map { it.toOrderItem() }
        return orderJpaRepository.save(Order(memberId, orderItems, amount, paymentResponse.id))
    }
}
