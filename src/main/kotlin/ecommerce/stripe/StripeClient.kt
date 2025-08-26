package ecommerce.stripe

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import ecommerce.dto.errors.StripeErrorMessage
import ecommerce.dto.errors.StripeErrorResponse
import ecommerce.dto.payment.PaymentRequest
import ecommerce.dto.payment.PaymentResponse
import ecommerce.exception.StripeClientException
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Configuration
class StripeClient(
    private val stripeProperties: StripeProperties,
) {
    private val restClient = RestClient.create()
    private val objectMapper = jacksonObjectMapper()

    fun createCheckoutSession(req: PaymentRequest): PaymentResponse? {
        val body =
            LinkedMultiValueMap<String, String>().apply {
                add("amount", req.amount.toString())
                add("currency", req.currency)
                add("payment_method", req.paymentMethod)
                add("confirm", "true")
                add("automatic_payment_methods[enabled]", "true")
                add("automatic_payment_methods[allow_redirects]", "never")
            }

        return try {
            val response =
                restClient.post()
                    .uri("https://api.stripe.com/v1/payment_intents")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ${stripeProperties.secretKey}")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .toEntity(PaymentResponse::class.java)

            response.body ?: throw IllegalStateException("EMpty response from Stripe")
        } catch (e: RestClientResponseException) {
            val stripeErrorResponse = parseStripeError(e.responseBodyAsString)
            val friendlyMessage = convertMessageFromError(stripeErrorResponse?.error)

            throw StripeClientException(
                StripeErrorResponse(
                    error =
                        StripeErrorMessage(
                            message = friendlyMessage,
                            code = stripeErrorResponse?.error?.code,
                            declineCode = stripeErrorResponse?.error?.declineCode,
                        ),
                ),
            )
        }
    }

    private fun parseStripeError(jsonString: String?): StripeErrorResponse? {
        if (jsonString.isNullOrBlank()) return null
        return try {
            objectMapper.readValue(jsonString)
        } catch (ex: Exception) {
            null
        }
    }

    private fun convertMessageFromError(error: StripeErrorMessage?): String {
        val declineCode = StripeDeclineCode.fromCode(error?.declineCode)

        return when (error?.code) {
            "card_declined" ->
                when (declineCode) {
                    StripeDeclineCode.INSUFFICIENT_FUNDS -> "Payment failed: insufficient balance."
                    StripeDeclineCode.EXPIRED_CARD -> "Payment failed: your card has expired."
                    StripeDeclineCode.INCORRECT_CVC -> "Payment failed: incorrect CVC."
                    StripeDeclineCode.LOST_CARD -> "Payment failed: lost card."
                    StripeDeclineCode.STOLEN_CARD -> "Payment failed: stolen card."
                    else -> "Payment was declined by your bank."
                }

            "expired_card" -> "Payment failed: your card has expired."
            "processing_error" -> "Payment failed due to a processing error."
            else -> error?.message ?: "Payment failed for an unknown reason."
        }
    }

    enum class StripeDeclineCode(val code: String) {
        INSUFFICIENT_FUNDS("insufficient_funds"),
        EXPIRED_CARD("expired_card"),
        INCORRECT_CVC("incorrect_cvc"),
        LOST_CARD("lost_card"),
        STOLEN_CARD("stolen_card"),
        UNKNOWN("unknown"), ;

        companion object {
            fun fromCode(code: String?): StripeDeclineCode = StripeDeclineCode.entries.find { it.code == code } ?: UNKNOWN
        }
    }
}
