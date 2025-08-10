package ecommerce.service

import ecommerce.dto.OrderPlacementRequest
import ecommerce.dto.OrderPlacementResponse
import ecommerce.dto.OrderResponseStatus
import ecommerce.dto.PaymentRequest
import ecommerce.dto.RegisteredMember
import ecommerce.exception.BadRequestException
import ecommerce.exception.NotFoundException
import ecommerce.model.Order
import ecommerce.model.OrderItem
import ecommerce.model.OrderStatus
import ecommerce.repository.CartRepository
import ecommerce.repository.MemberRepository
import ecommerce.repository.OptionRepository
import ecommerce.repository.OrderItemRepository
import ecommerce.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val paymentService: PaymentService,
    private val optionRepository: OptionRepository,
    private val cartRepository: CartRepository,
    private val memberRepository: MemberRepository,
) {
    fun placeOrder(
        req: OrderPlacementRequest,
        loginMember: RegisteredMember,
    ): OrderPlacementResponse {
        val option =
            optionRepository.findById(req.productOptionId)
                .orElseThrow { NotFoundException("Option not found") }
        if (!option.isAvailable(req.quantity)) {
            throw BadRequestException("Out of stock")
        }

        val member =
            memberRepository.findByEmail(loginMember.email)
                ?: throw NotFoundException("User doesn't exists")
        val cart = cartRepository.findCartByMemberId(loginMember.id)
        val product = option.product

        val totalAmount = (product.price * req.quantity)
        val item = OrderItem(req.quantity, product.name, option.name)
        val order =
            orderRepository.save(
                Order(
                    member = member,
                    items = mutableListOf(item),
                    paymentAmount = totalAmount,
                    paymentMethod = req.paymentMethod,
                    status = OrderStatus.PENDING,
                ),
            )

        // TODO: check if PaymentRequest should take arguments from order or request
        try {
            val sessionId = paymentService.createPaymentIntent(PaymentRequest(totalAmount, "usd", req.paymentMethod))
            order.status = OrderStatus.PAID
            order.checkoutSessionId = sessionId
            orderRepository.save(order)
        } catch (e: Exception) {
            order.status = OrderStatus.FAILED
            orderRepository.save(order)
            throw e
        }

        try {
            cart?.removeItem(product, req.quantity)
        } catch (e: Exception) {
        }
        option.decreaseQuantity(req.quantity)

        return OrderPlacementResponse(
            status = OrderResponseStatus.SUCCESS.name,
            orderId = order.id,
            message = "Payment successful. Order has been placed",
        )
    }
}
