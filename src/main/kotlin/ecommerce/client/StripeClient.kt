package ecommerce.client

import ecommerce.config.StripeProperties
import ecommerce.dto.PaymentRequest
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
    fun createCheckoutSession(req: PaymentRequest): String? {
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
//                    .onStatus(HttpStatusCode::is4xxClientError) { response, errorBody ->
//                        throw BadRequestException("An error occurred during payment: ${errorBody.body}")
//                    }
//                    .onStatus(HttpStatusCode::is5xxServerError) { response, errorBody ->
//                        throw ExternalServiceException("Stripe down: $errorBody")
//                    }
                    .toEntity(String::class.java)

            response.body
        } catch (e: RestClientResponseException) {
            val errorBody = e.responseBodyAsString
            val statusCode = e.statusCode

            throw when {
                statusCode.is4xxClientError -> BadRequestException("An error occurred during payment: $errorBody")
                statusCode.is5xxServerError -> ExternalServiceException("Stripe down: $errorBody")
                else -> IllegalArgumentException("Unexpected error: ${e.message}")
            }
        }
    }
}
