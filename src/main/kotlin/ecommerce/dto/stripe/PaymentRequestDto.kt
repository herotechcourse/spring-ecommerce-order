package ecommerce.dto.stripe

data class PaymentRequestDto(
    val amount: Double,
    val currency: String,
    val paymentMethodId: String,
)
