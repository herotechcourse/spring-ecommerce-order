package ecommerce.dto

import ecommerce.enum.Currency
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class OrderRequest(
    @field:NotNull(message = "Currency is required")
    val currency: Currency,
    @field:Pattern(
        regexp = "^pm_.*$",
        message = "Payment method must start with 'pm_'",
    )
    @field:NotBlank(message = "Payment method is required")
    val paymentMethod: String,
)
