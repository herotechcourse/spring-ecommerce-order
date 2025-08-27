package ecommerce.dto.stripe

data class PaymentRequest(
    val currency: String,
    val paymentMethod: String,
    var amount: Int = 0,
)
