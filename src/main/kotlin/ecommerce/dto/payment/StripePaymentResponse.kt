package ecommerce.dto.payment

import com.fasterxml.jackson.annotation.JsonProperty

data class StripePaymentResponse(
    val id: String,
    val status: String,
    val amount: Long,
    val currency: String,
    @JsonProperty("last_payment_error")
    val lastPaymentError: StripeErrorDetails? = null,
)

data class StripeErrorDetails(
    val code: String?,
    val message: String?,
)

data class StripePaymentIntentRequest(
    val amount: Long,
    val currency: String = "usd",
    val confirm: Boolean = true,
    @get:JsonProperty("automatic_payment_methods[enabled]")
    val automaticPaymentMethodsEnabled: Boolean = true,
    @get:JsonProperty("automatic_payment_methods[allow_redirects]")
    val automaticPaymentMethodsAllowRedirects: String = "never",
)

data class StripeErrorResponse(
    val error: StripeErrorDetails,
)
