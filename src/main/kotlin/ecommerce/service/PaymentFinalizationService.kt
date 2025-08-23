package ecommerce.service

import ecommerce.dto.PaymentResponse
import ecommerce.dto.PlaceOrderResponse
import ecommerce.entity.enumerated.OrderStatus
import ecommerce.entity.enumerated.PaymentAttemptStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PaymentFinalizationService(
    private val orderService: OrderService,
    private val paymentAttemptService: PaymentAttemptService,
    private val optionService: OptionService,
) {
    @Transactional
    fun finalizePaid(
        orderId: Long,
        paymentAttemptId: Long,
    ): PlaceOrderResponse {
        orderService.updateOrderStatus(orderId, OrderStatus.PAID)
        paymentAttemptService.updateStatus(paymentAttemptId, PaymentAttemptStatus.APPROVED)
        return PlaceOrderResponse(
            orderId,
            LocalDateTime.now(),
            OrderStatus.PAID,
            orderService.findById(orderId).totalAmount,
        )
    }

    @Transactional
    fun finalizeFailed(
        orderId: Long,
        paymentAttemptId: Long,
        paymentResponse: PaymentResponse?,
    ): PlaceOrderResponse {
        orderService.updateOrderStatus(orderId, OrderStatus.FAILED)
        paymentAttemptService.updateStatus(paymentAttemptId, PaymentAttemptStatus.REJECTED)

        val order = orderService.findById(orderId)
        val itemsToRestore = order.items.map { it.optionId to it.quantity }

        itemsToRestore.forEach { (optionId, quantity) ->
            optionService.increaseQuantity(optionId, quantity)
        }

        return PlaceOrderResponse(
            orderId,
            LocalDateTime.now(),
            OrderStatus.FAILED,
            order.totalAmount,
            paymentResponse?.id,
        )
    }
}
