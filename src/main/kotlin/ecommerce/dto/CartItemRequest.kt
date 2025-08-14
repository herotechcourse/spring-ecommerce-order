package ecommerce.dto

import jakarta.validation.constraints.Positive

class CartItemRequest(
    @field:Positive(message = "Product ID must be positive")
    val optionId: Long,
    @field:Positive(message = "Quantity must be positive")
    val quantity: Int,
)
