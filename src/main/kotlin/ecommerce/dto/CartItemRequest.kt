package ecommerce.dto

import jakarta.validation.constraints.Positive

class CartItemRequest(
    @field:Positive(message = "Product ID must be positive")
    val productId: Long,
    @field:Positive(message = "Quantity must be positive")
    val quantity: Int,
    val optionId: Long,
)
