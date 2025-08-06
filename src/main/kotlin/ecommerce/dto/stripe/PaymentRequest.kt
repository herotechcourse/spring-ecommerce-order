package ecommerce.dto.stripe

class PaymentRequest(
    val amount: Int,
    val currency: String,
    val paymentMethod: String,
)
