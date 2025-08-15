package ecommerce.services.order

import ecommerce.controller.order.usecase.OrderCreationUseCase
import ecommerce.dto.MemberLoginDTO
import ecommerce.dto.OrderDTO
import ecommerce.dto.PaymentDTO
import ecommerce.entities.CartItemEntity
import ecommerce.entities.MemberEntity
import ecommerce.entities.OrderEntity
import ecommerce.entities.OrderItemEntity
import ecommerce.entities.PaymentEntity
import ecommerce.exception.NotFoundException
import ecommerce.exception.OperationFailedException
import ecommerce.mappers.toDTO
import ecommerce.model.PaymentRequest
import ecommerce.model.StripeResponse
import ecommerce.repositories.CartItemRepository
import ecommerce.repositories.MemberRepository
import ecommerce.repositories.OrderRepository
import ecommerce.services.payment.PaymentService
import ecommerce.services.stripe.StripeService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderServiceImpl(
    private val stripeService: StripeService,
    private val cartItemRepository: CartItemRepository,
    private val memberRepository: MemberRepository,
    private val orderRepository: OrderRepository,
    private val paymentService: PaymentService,
) : OrderCreationUseCase {
    @Transactional
    override fun create(
        memberLoginDTO: MemberLoginDTO,
        paymentRequest: PaymentRequest,
    ): OrderDTO {
        val member =
            memberRepository.findByIdOrNull(memberLoginDTO.id)
                ?: throw NotFoundException("Member not found")
        val cartItems = cartItemRepository.findByMemberId(member.id)
        if (cartItems.isEmpty()) throw OperationFailedException("Cart is empty")

        validateStock(cartItems)

        val stripeResponse =
            stripeService.createPaymentIntent(
                paymentRequest.copy(
                    amount = cartItems.sumOf { it.option.totalPrice },
                ),
            )

        val order = buildOrderEntity(member, cartItems, stripeResponse)
        val savedOrder = orderRepository.save(order)
        val paymentDTO = paymentService.createPayment(savedOrder, stripeResponse)

        processPaymentOutcome(paymentDTO, cartItems)
        return savedOrder.toDTO()
    }

    private fun buildOrderEntity(
        member: MemberEntity,
        cartItems: List<CartItemEntity>,
        stripeResponse: StripeResponse,
    ): OrderEntity {
        val order =
            OrderEntity(
                status = OrderEntity.OrderStatus.CREATED,
                totalAmount = stripeResponse.amount!!,
                member = member,
            )
        val orderItems = cartItems.map { OrderItemEntity(order, it.option, it.quantity, it.option.unitPrice) }
        order.addAllItems(orderItems)
        return order
    }

    private fun processPaymentOutcome(
        payment: PaymentDTO,
        cartItems: List<CartItemEntity>,
    ) {
        when (payment.status) {
            PaymentEntity.PaymentStatus.SUCCEEDED -> {
                decreaseOptionStock(cartItems)
                cartItemRepository.deleteAll(cartItems)
            }

            PaymentEntity.PaymentStatus.PROCESSING -> decreaseOptionStock(cartItems)
            else -> return
        }
    }

    private fun decreaseOptionStock(cartItems: List<CartItemEntity>) {
        cartItems.forEach { it.option.subtract(it.quantity) }
    }

    private fun validateStock(cartItems: List<CartItemEntity>) {
        cartItems.forEach { it.option.validateStock(it.quantity) }
    }
}
