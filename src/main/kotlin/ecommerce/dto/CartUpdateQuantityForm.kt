package ecommerce.dto

import jakarta.validation.constraints.Min

data class CartUpdateQuantityForm(
    @field:Min(value = 1, message = "Product quantity is too small")
    var quantity: Int,
)
