package ecommerce.dto.payment

import com.fasterxml.jackson.annotation.JsonProperty

data class PaymentResponse(
    val id: String,
    val status: String,
    val amount: Long? = null,
    @JsonProperty("stripe_error")
    val clientSecret: String? = null,
    val errorMessage: String? = null,
)
