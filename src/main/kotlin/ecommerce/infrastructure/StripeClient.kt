package ecommerce.infrastructure

import com.stripe.exception.CardException
import ecommerce.dto.payment.PaymentBody
import ecommerce.dto.payment.PaymentRequest
import ecommerce.dto.stripe.StripeResponse
import ecommerce.utils.exception.StripeException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

@Component
class StripeClient(
    @Value("\${stripe.secret-key}")
    private val stripeKey: String,
    private val stripeRestClient: RestClient,
) {
    private val logger = ApplicationLogger()

    fun createCheckoutSession(req: PaymentRequest): StripeResponse? {
        val body =
            PaymentBody(
                req.amount,
                req.currency,
                req.paymentMethod,
            ).toString()

        return try {
            val response =
                stripeRestClient.post()
                    .uri("https://api.stripe.com/v1/payment_intents")
                    .headers { headers ->
                        headers.setBearerAuth(stripeKey)
                        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
                    }
                    .body(body)
                    .retrieve()
                    .toEntity(StripeResponse::class.java)

            response.body
        } catch (e: RestClientException) {
            throw handleError(e)
        }
    }

    fun confirmPayment(intentId: String): StripeResponse? {
        return try {
            val response =
                stripeRestClient.post()
                    .uri("https://api.stripe.com/v1/payment_intents/$intentId/confirm")
                    .headers { headers ->
                        headers.setBearerAuth(stripeKey)
                        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
                    }
                    .retrieve()
                    .toEntity(StripeResponse::class.java)

            response.body
        } catch (e: Exception) {
            throw handleError(e)
        }
    }

    private fun handleError(e: Exception): StripeException {
        logger.logError("Error from Stripe API: ${e.message}")
        when (e) {
            is CardException -> {
                return StripeException("Invalid request: ${e.message}")
            }
            is HttpClientErrorException.BadRequest -> {
                val errorBody = e.responseBodyAsString
                return StripeException("Invalid request: $errorBody")
            }
            is HttpClientErrorException.Unauthorized -> {
                return StripeException("Authentication failed: invalid Stripe key error: ${e.message}")
            }
            is HttpServerErrorException -> {
                return StripeException("Stripe service is unavailable error: ${e.message}")
            }
            else -> {
                return StripeException("An unexpected error occurred with the Stripe API error: ${e.message}")
            }
        }
    }
}
