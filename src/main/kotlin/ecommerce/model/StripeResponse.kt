package ecommerce.model

data class StripeResponse(
    val id: String?,
    val amount: Long?,
    val currency: String?,
    val status: String?,
    val cancellationReason: String?,
    val clientSecret: String?,
    val customer: String?,
    val failureCode: String? = null,
    val failureMessage: String? = null,
    val created: String? = null,
    val paymentMethod: String?,
    val latestCharge: String?,
) {
    val amountDecimal: Double
        get() = amount?.div(100.0) ?: 0.0
}
