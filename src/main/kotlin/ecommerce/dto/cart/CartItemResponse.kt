package ecommerce.dto.cart

import ecommerce.dto.product.ProductResponse
import java.time.LocalDateTime

data class CartItemResponse(
    val product: ProductResponse,
    val quantity: Int,
    val createdAt: LocalDateTime,
)
