package ecommerce.dto.stripe

import java.time.LocalDateTime

data class PaymentDto(
    val amount: Int,
    val currency: String,
    val status: String,
    val createdAt: LocalDateTime,
)
