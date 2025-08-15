package ecommerce.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class PaymentRequest(
    @field:Positive
    var amount: Double,
    @field:NotBlank
    val currency: String,
    @field:NotBlank
    val paymentMethod: String,
) {
    val amountInSmallestUnit: Long
        get() = (amount * 100).toLong()
}
