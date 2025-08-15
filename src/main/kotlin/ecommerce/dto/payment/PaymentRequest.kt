package ecommerce.dto.payment

class PaymentRequest(
    val amount: String,
    val currency: String = DEFAULT_CURRENCY,
    val paymentMethod: String = DEFAULT_PAYMENT_METHOD,
) {
    companion object {
        private const val DEFAULT_CURRENCY = "usd"
        private const val DEFAULT_PAYMENT_METHOD = "pm_card_visa"
    }
}
