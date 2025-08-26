package ecommerce.dto.payment

data class PaymentRequest(
    val amount: Double,
    val currency: String,
    val paymentMethod: String,
)
