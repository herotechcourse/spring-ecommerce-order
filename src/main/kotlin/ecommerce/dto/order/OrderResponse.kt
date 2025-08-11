package ecommerce.dto.order

import ecommerce.entity.OrderStatus
import java.time.LocalDateTime

data class OrderResponse(
    val id: Long,
    val orderedAt: LocalDateTime,
    val status: OrderStatus,
    val totalAmount: Long,
    val currency: String,
    val paymentIntentId: String?,
    val paymentStatus: String?,
    val items: List<OrderItemResponse>,
)
