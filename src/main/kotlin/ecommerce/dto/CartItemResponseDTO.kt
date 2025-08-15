package ecommerce.dto

import java.time.LocalDateTime

data class CartItemResponseDTO(
    val id: Long,
    val memberId: Long,
    val option: OptionDTO,
    val quantity: Int,
    val addedAt: LocalDateTime,
)
