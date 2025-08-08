package ecommerce.dto

import ecommerce.model.Product
import java.time.LocalDateTime

data class CartItemResponse(
    val product: Product,
    val quantity: Int,
    val createdAt: LocalDateTime,
)
