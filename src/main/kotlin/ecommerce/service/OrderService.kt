package ecommerce.service

import ecommerce.dto.OrderDto
import ecommerce.dto.OrderPlacementRequest
import ecommerce.dto.OrderPlacementResponse
import ecommerce.dto.OrderResponseStatus
import ecommerce.dto.RegisteredMember
import ecommerce.exception.BadRequestException
import ecommerce.exception.NotFoundException
import ecommerce.model.Cart
import ecommerce.model.Member
import ecommerce.model.Option
import ecommerce.model.Order
import ecommerce.model.OrderItem
import ecommerce.model.OrderStatus
import ecommerce.repository.CartRepository
import ecommerce.repository.MemberRepository
import ecommerce.repository.OptionRepository
import ecommerce.repository.OrderRepository
import ecommerce.service.mapper.OrderMapper.toOrderDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val paymentService: PaymentService,
    private val optionRepository: OptionRepository,
    private val cartRepository: CartRepository,
    private val memberRepository: MemberRepository,
) {
    fun placeOrder(
        req: OrderPlacementRequest,
        loginMember: RegisteredMember,
    ): OrderPlacementResponse {
        val option = findAndValidateOption(req)

        val member = findMember(loginMember)
        val cart = cartRepository.findCartByMemberId(loginMember.id)

        val totalAmount = (option.product.price * req.quantity)
        val order = createPendingOrder(req, option, member, totalAmount)

        paymentService.processPayment(order) // NOTE: this starts a new transaction, so order will be saved even if it fails
        updateStockAndCart(cart, option, req.quantity)

        return OrderPlacementResponse(
            status = OrderResponseStatus.SUCCESS.name,
            orderId = order.id,
            message = "Payment successful. Order has been placed",
        )
    }

    fun getAllOrdersForMember(memberId: Long): List<OrderDto> {
        val orders = orderRepository.findAllByMemberId(memberId)
        return orders.map { order -> order.toOrderDto() }
    }

    private fun findAndValidateOption(req: OrderPlacementRequest): Option {
        val option =
            optionRepository.findById(req.productOptionId)
                .orElseThrow { NotFoundException("Option not found") }
        if (!option.isAvailable(req.quantity)) {
            throw BadRequestException("Out of stock")
        }
        return option
    }

    private fun findMember(loginMember: RegisteredMember): Member {
        return memberRepository.findById(loginMember.id)
            .orElseThrow { NotFoundException("Member not found") }
    }

    private fun createPendingOrder(
        req: OrderPlacementRequest,
        option: Option,
        member: Member,
        totalAmount: Double,
    ): Order {
        val item = OrderItem(req.quantity, option.product.name, option.name)
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
        return order
    }

    private fun updateStockAndCart(
        cart: Cart?,
        option: Option,
        quantity: Int,
    ) {
        try {
            cart?.removeItem(option.product, quantity)
        } catch (e: Exception) {
        }
        option.decreaseQuantity(quantity)
    }
}
