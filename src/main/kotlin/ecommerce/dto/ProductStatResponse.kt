package ecommerce.dto

import java.time.LocalDateTime

data class ProductStatResponse(
    val name: String,
    val count: Long,
    val lastAddedAt: LocalDateTime,
)
