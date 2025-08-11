package ecommerce.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ProductRequest(
    @field:NotBlank(message = "Name must not be blank")
    val name: String,
    @field:Min(value = 1, message = "Price must be greater than 0")
    val price: Int,
    @field:NotBlank(message = "Image URL must not be blank")
    val imageUrl: String,
    @field:Size(min = 1, message = "At least one option is required")
    val options: List<OptionRequest>,
)
