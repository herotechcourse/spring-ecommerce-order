package ecommerce.dto

class PaymentRequest(
    val amount: Double,
    val currency: String,
    val paymentMethod: String,
)
