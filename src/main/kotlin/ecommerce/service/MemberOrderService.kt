package ecommerce.service

import ecommerce.dto.cartProduct.CartProductDto
import ecommerce.dto.order.OrderIntentResponse
import ecommerce.dto.order.OrderProductResponse
import ecommerce.dto.order.OrderResponse
import ecommerce.dto.payment.PaymentRequest
import ecommerce.dto.stripe.StripeResponse
import ecommerce.enums.OrderStatus
import ecommerce.infrastructure.StripeClient
import ecommerce.model.MemberOrder
import ecommerce.model.OrderProduct
import ecommerce.model.User
import ecommerce.repository.OptionRepository
import ecommerce.repository.UserRepository
import ecommerce.utils.exception.EntityNotFoundException
import ecommerce.utils.exception.LowStockException
import ecommerce.utils.exception.StripeException
import ecommerce.utils.helper.OrderUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberOrderService(
    val orderService: OrderService,
    val optionRepository: OptionRepository,
    val cartService: CartService,
    val stripeClient: StripeClient,
    val userRepository: UserRepository,
) {
    fun getUserOrders(userId: Long): List<OrderResponse> {
        return orderService.getUserOrders(userId).map { it.toOrderResponse() }
    }

    @Transactional(readOnly = true)
    fun getOrderById(orderId: Long): OrderResponse {
        return orderService.getOrderById(orderId).toOrderResponse()
    }

    fun createCheckoutCartIntent(userId: Long): OrderIntentResponse {
        val member = getUser(userId)
        val cartProducts = getCartProducts(member)
        checkCartProducts(cartProducts)
        val paymentRequest = createPaymentRequest(cartProducts)
        val paymentIntentId = stripeClient.createCheckoutSession(paymentRequest)!!.id
        val order = orderService.createOrder(member, cartProducts, paymentIntentId)
        return OrderIntentResponse(paymentIntentId, order.id)
    }

    fun confirmCheckout(orderId: Long): StripeResponse? {
        val order = orderService.getOrderById(orderId)
        val member = getUser(order.userId)
        val cartProducts = getCartProducts(member)
        checkCartProducts(cartProducts)
        if (order.status == OrderStatus.REJECTED) {
            order.incrementAttempt()
        }
        return try {
            val response = stripeClient.confirmPayment(order.paymentId)
            cartService.checkoutCart(member)
            order.changeStatus(OrderStatus.COMPLETED)
            response
        } catch (e: StripeException) {
            order.changeStatus(OrderStatus.REJECTED)
            throw StripeException(e.message)
        }
    }

    private fun checkCartProducts(cartProducts: List<CartProductDto>) {
        cartProducts.forEach {
            val option =
                optionRepository.findById(it.optionId).orElseThrow {
                    throw EntityNotFoundException("Option with id ${it.optionId} not found")
                }
            if (option.quantity < it.quantity) {
                throw LowStockException("Option with id ${it.optionId} is low on stock")
            }
        }
    }

    private fun createPaymentRequest(cartProductDto: List<CartProductDto>): PaymentRequest {
        return PaymentRequest(
            (OrderUtil.calculateTotal(cartProductDto) * 100).toInt().toString(),
        )
    }

    private fun getCartProducts(member: User): List<CartProductDto> {
        return cartService.getCartProducts(member).products.takeIf { it.isNotEmpty() }
            ?: throw EntityNotFoundException("No products found")
    }

    private fun getUser(userId: Long): User {
        return userRepository.findById(userId).orElseThrow { throw EntityNotFoundException("User with id $userId not found") }
    }

    private fun MemberOrder.toOrderResponse(): OrderResponse {
        return OrderResponse(
            id,
            createdAt,
            status,
            totalAmount,
            paymentId,
            paymentOption,
            optionProducts.map { it.toResponse() },
        )
    }

    private fun OrderProduct.toResponse(): OrderProductResponse {
        return OrderProductResponse(
            price,
            quantity,
            optionRepository.findById(optionId).orElse(null)?.imageUrl,
        )
    }
}
