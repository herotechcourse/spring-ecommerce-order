package ecommerce.stripe

import com.fasterxml.jackson.databind.ObjectMapper
import ecommerce.dto.PaymentRequest
import ecommerce.dto.PaymentResponse
import ecommerce.dto.StripeErrorDetail
import ecommerce.dto.StripeErrorResponse
import ecommerce.exception.PaymentDeclinedException
import ecommerce.exception.PaymentProviderErrorException
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient

@Component
class StripeClient(
    private val stripeProperties: StripeProperties,
    private val restClient: RestClient,
    private val objectMapper: ObjectMapper,
) {
    fun createCheckoutSession(req: PaymentRequest): PaymentResponse? {
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

            response.body
        } catch (e: HttpClientErrorException) {
            val stripeError = parseStripeError(e.responseBodyAsString)
            val userMessage =
                when (stripeError.decline_code ?: stripeError.code) {
                    "insufficient_funds" -> "Insufficient funds on the card."
                    "card_declined" -> "Your card was declined. Please try another card."
                    "incorrect_cvc" -> "Incorrect CVC code."
                    "expired_card" -> "The card has expired."
                    else -> "Payment failed. Please check your details or try again later."
                }
            throw PaymentDeclinedException(userMessage)
        } catch (e: Exception) {
            throw PaymentProviderErrorException("Unknown error from payment provider.")
        }
    }

    private fun parseStripeError(responseBody: String): StripeErrorDetail {
        return try {
            objectMapper.readValue(responseBody, StripeErrorResponse::class.java).error
        } catch (e: Exception) {
            StripeErrorDetail(null, null, "Unknown error from payment provider.")
        }
    }
}
