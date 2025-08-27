package ecommerce.dto

import ecommerce.dto.stripe.PaymentDto
import ecommerce.model.OrderStatus
import java.time.LocalDateTime

data class OrderResponse(
    val orderId: Long,
    val memberId: Long,
    val orderDate: LocalDateTime,
    val status: OrderStatus,
    val items: List<OrderItemResponse>,
    val payment: PaymentDto?,
)
