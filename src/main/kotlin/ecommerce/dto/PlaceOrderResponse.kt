package ecommerce.dto

import ecommerce.entity.enumerated.OrderStatus
import java.time.LocalDateTime

class PlaceOrderResponse(
    val orderId: Long,
    val createdAt: LocalDateTime,
    val status: OrderStatus,
    val totalAmount: Int,
    val paymentId: String? = null,
)
