package ecommerce.dto

data class StripePaymentResponse(
    val id: String,
    val amount: Int,
    val status: String,
)
