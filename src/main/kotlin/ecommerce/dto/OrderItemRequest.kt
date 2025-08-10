package ecommerce.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class OrderItemRequest(
    @field:NotNull
    val optionId: Long,

    @field:Min(1)
    val quantity: Int,
)
