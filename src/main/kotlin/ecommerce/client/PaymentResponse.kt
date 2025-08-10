package ecommerce.client

data class PaymentResponse(
    val id: String,
    val `object`: String,
    val amount: Int,
    val payment_method: String,
    val client_secret: String,
    val confirmation_method: String,
    val status: String,
    val next_action: String?,
)
