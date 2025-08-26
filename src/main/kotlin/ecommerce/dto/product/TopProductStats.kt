package ecommerce.dto.product

import java.time.LocalDateTime

data class TopProductStats(
    val name: String,
    val addCount: Int,
    val lastAddedAt: LocalDateTime,
)
