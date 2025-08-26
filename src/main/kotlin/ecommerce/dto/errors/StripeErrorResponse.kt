package ecommerce.dto.errors

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StripeErrorResponse(
    val error: StripeErrorMessage,
)

@Serializable
data class StripeErrorMessage(
    val message: String,
    val code: String? = null,
    @SerialName("decline_code") val declineCode: String? = null,
)
