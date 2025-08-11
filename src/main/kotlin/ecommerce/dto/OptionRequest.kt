package ecommerce.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class OptionRequest(
    @field:NotBlank
    val name: String,
    @field:Positive
    val quantity: Int,
)
