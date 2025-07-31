package ecommerce.model

import java.time.LocalDateTime

data class CartItem(
    val id: Long,
    val memberId: Long,
    val productId: Long,
    val createdAt: LocalDateTime,
)
