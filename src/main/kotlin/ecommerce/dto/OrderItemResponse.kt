package ecommerce.dto

import java.time.LocalDateTime

data class OrderItemResponse(
    val itemId: Long,
    val productName: String,
    val optionName: String,
    val quantity: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
