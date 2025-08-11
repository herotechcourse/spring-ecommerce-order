package ecommerce.dto

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

data class StripeErrorResponse(val error: StripeError)

data class StripeError(
    val code: String,
    val docUrl: String,
    val message: String,
    val param: String?,
    val requestLogUrl: String,
    val type: String,
)

// TODO make it work
fun parseStripeError(rawMessage: String): String {
    val jsonPart = rawMessage.trim()

    val mapper = jacksonObjectMapper()

    try {
        val stripeErrorResponse = mapper.readValue<StripeErrorResponse>(jsonPart)
        val error = stripeErrorResponse.error
        val nicerMessage =
            """
            Stripe Error:
            Type: ${error.type}
            Code: ${error.code}
            Parameter: ${error.param ?: "N/A"}
            Message: ${error.message}
            Documentation: ${error.docUrl}
            Request log: ${error.requestLogUrl}
            """.trimIndent()

        return nicerMessage
    } catch (e: Exception) {
        return rawMessage
    }
}
