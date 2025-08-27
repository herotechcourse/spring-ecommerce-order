package ecommerce.dto.stripe

import com.fasterxml.jackson.annotation.JsonProperty

data class StripeErrorResponse(
    val error: StripeErrorDetail? = null,
)

data class StripeErrorDetail(
    val code: String? = null,
    @JsonProperty("decline_code")
    val declineCode: String? = null,
    val message: String? = null,
)
