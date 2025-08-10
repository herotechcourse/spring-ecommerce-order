package ecommerce.dto

import jakarta.validation.constraints.Positive

data class CartRequest(
    @field:Positive val productId: Long,
    @field:Positive val optionId: Long,
    @field:Positive val cartId: Long,
    @field:Positive val quantity: Int = 1,
)
