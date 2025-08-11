package ecommerce.dto

import ecommerce.model.OrderStatus
import java.time.LocalDateTime

data class OrderResponse(
    val orderData: LocalDateTime,
    val orderStatus: OrderStatus,
    val orderItems: List<OrderItemResponse>,
    val checkoutSessionId: String?,
    val paymentAmount: Double,
)
