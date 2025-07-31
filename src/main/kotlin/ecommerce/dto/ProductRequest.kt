package ecommerce.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ProductRequest(
    @field:NotBlank(message = "Name must not be blank")
    @field:Size(max = 15, message = "Name must be 15 characters or fewer")
    @field:Pattern(
        regexp = """^[\w\s\[\]\(\)\+\-\&/_]+$""",
        message = "Name contains invalid characters",
    )
    val name: String,
    @field:Min(value = 1, message = "Price must be greater than 0")
    val price: Double,
    @field:NotBlank(message = "Image URL must not be blank")
    @field:Pattern(
        regexp = """^https?://.*""",
        message = "Image URL must start with http:// or https://",
    )
    val imageUrl: String,
)
