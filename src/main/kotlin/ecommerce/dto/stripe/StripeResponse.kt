package ecommerce.dto.stripe

class StripeResponse(
    val id: String,
    val amount: Int,
    val status: String,
    val created: Int,
    val currency: String,
)
