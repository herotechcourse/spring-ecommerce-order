package ecommerce.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

class OrderPlacementRequest(
    val productOptionId: Long,
    @field:Min(value = 1, message = "Please choose a product")
    val quantity: Int,
    @field:NotBlank(message = "Please provide a payment method")
    val paymentMethod: String,
)
