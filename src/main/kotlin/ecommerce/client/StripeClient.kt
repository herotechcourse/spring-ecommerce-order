package ecommerce.client

import ecommerce.config.StripeProperties
import ecommerce.dto.PaymentIntentResponse
import ecommerce.dto.PaymentRequest
import ecommerce.dto.mapStringToStripeErrorResponse
import ecommerce.dto.toPaymentIntentResponse
import ecommerce.exception.BadRequestException
import ecommerce.exception.ExternalServiceException
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Component
class StripeClient(
    private val stripeProperties: StripeProperties,
    private val restClient: RestClient,
) {
    fun createCheckoutSession(req: PaymentRequest): PaymentIntentResponse {
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
                    .toEntity(String::class.java)

            val responseBody = response.body ?: throw ExternalServiceException("Empty response from Stripe")
            responseBody.toPaymentIntentResponse()
        } catch (e: RestClientResponseException) {
            val errorBody = e.responseBodyAsString
            val statusCode = e.statusCode

            val stripeError = mapStringToStripeErrorResponse(errorBody)

            throw when {
                statusCode.is4xxClientError -> BadRequestException("An error occurred during payment: ${stripeError.error.message}")
                statusCode.is5xxServerError -> ExternalServiceException("Stripe down: ${stripeError.error.message}")
                else -> IllegalArgumentException("Unexpected error: ${e.message}")
            }
        }
    }
}
