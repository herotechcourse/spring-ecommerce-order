package ecommerce.dto.order

import java.time.LocalDateTime

data class OrderResponseDto(
    val orderId: Long,
    val createdAt: LocalDateTime,
    val status: String,
    val checkoutSessionId: String,
    val amount: Double,
    val currency: String,
    val paymentMethod: String,
    val items: List<OrderItemResponseDto>,
)
