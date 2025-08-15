package ecommerce.dto.payment

data class PaymentBody(
    val amount: String,
    val currency: String,
    val paymentMethod: String,
    val confirm: Boolean = false,
    val automaticPaymentMethodsEnabled: Boolean = true,
    val automaticPaymentMethodsAllowRedirects: String = "never",
) {
    override fun toString(): String {
        return listOf(
            "amount=$amount",
            "currency=$currency",
            "payment_method=$paymentMethod",
            "confirm=$confirm",
            "automatic_payment_methods[enabled]=$automaticPaymentMethodsEnabled",
            "automatic_payment_methods[allow_redirects]=$automaticPaymentMethodsAllowRedirects",
        ).joinToString("&")
    }
}
