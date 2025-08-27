package ecommerce.stripe

import com.fasterxml.jackson.databind.ObjectMapper
import ecommerce.dto.stripe.PaymentRequest
import ecommerce.dto.stripe.PaymentResponse
import ecommerce.dto.stripe.StripeErrorDetail
import ecommerce.dto.stripe.StripeErrorResponse
import ecommerce.exception.StripeResponseParsingException
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import kotlin.collections.joinToString
import kotlin.jvm.java

@Component
class StripeClient(
    private val stripeProperties: StripeProperties,
    private val objectMapper: ObjectMapper,
) {
    private val restClient: RestClient = RestClient.create()

    fun createCheckoutSession(req: PaymentRequest): PaymentResponse {
        val body =
            listOf(
                "amount=${req.amount}",
                "currency=${req.currency}",
                "payment_method=${req.paymentMethod}",
                "confirm=true",
                "automatic_payment_methods[enabled]=true",
                "automatic_payment_methods[allow_redirects]=never",
            ).joinToString("&")

        return try {
            val response =
                restClient.post()
                    .uri("https://api.stripe.com/v1/payment_intents")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ${stripeProperties.secretKey}")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .toEntity(PaymentResponse::class.java)

            response.body ?: throw IllegalStateException("Empty response from Stripe")
        } catch (e: RestClientResponseException) {
            val errorJson = e.responseBodyAsString
            val stripeError = parseStripeError(errorJson)
            PaymentResponse(
                id = "",
                amount = req.amount,
                status = "FAILED",
                clientSecret = null,
                errorMessage = convertMessageFromError(stripeError?.error),
            )
        } catch (e: Exception) {
            throw IllegalArgumentException("Stripe error: ${e.message}", e)
        }
    }

    private fun parseStripeError(json: String?): StripeErrorResponse? {
        if (json.isNullOrBlank()) return null
        return try {
            objectMapper.readValue(json, StripeErrorResponse::class.java)
        } catch (ex: Exception) {
            throw StripeResponseParsingException("Failed to parse Stripe error JSON. Raw JSON: $json", ex)
        }
    }

    private fun convertMessageFromError(error: StripeErrorDetail?): String {
        return when (error?.code) {
            "card_declined" ->
                when (error.declineCode) {
                    "insufficient_funds" -> "Payment failed: insufficient balance."
                    "expired_card" -> "Payment failed: your card has expired."
                    "incorrect_cvc" -> "Payment failed: incorrect CVC."
                    "lost_card" -> "Payment failed: lost card."
                    "stolen_card" -> "Payment failed: stolen card."
                    else -> "Payment was declined by your bank."
                }
            "expired_card" -> "Payment failed: your card has expired."
            "processing_error" -> "Payment failed due to a processing error."
            else -> error?.message ?: "Payment failed for an unknown reason."
        }
    }
}
