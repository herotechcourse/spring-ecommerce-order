package ecommerce.service

import ecommerce.dto.PlaceOrderRequest
import ecommerce.entity.Order
import ecommerce.entity.PaymentAttempt
import ecommerce.entity.enumerated.PaymentAttemptStatus
import ecommerce.repository.PaymentAttemptJpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderCreationService(
    private val orderService: OrderService,
    private val paymentAttemptRepo: PaymentAttemptJpaRepository,
    private val memberService: MemberService,
    private val optionService: OptionService,
) {
    @Transactional
    fun createPending(
        memberId: Long,
        req: PlaceOrderRequest,
    ): Pair<Order, PaymentAttempt> {
        req.orderItems.forEach { item ->
            optionService.decreaseQuantity(item.optionId, item.quantity)
        }

        val order = orderService.create(memberService.findById(memberId), req.orderItems)
        val attempt =
            paymentAttemptRepo.save(
                PaymentAttempt(
                    order = order,
                    status = PaymentAttemptStatus.PENDING,
                ),
            )

        return order to attempt
    }
}
