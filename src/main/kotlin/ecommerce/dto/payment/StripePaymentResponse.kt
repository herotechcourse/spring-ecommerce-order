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
