package ecommerce.service.payment

import com.fasterxml.jackson.databind.ObjectMapper
import ecommerce.config.StripeProperties
import ecommerce.handler.StripeConnectionException
import ecommerce.handler.StripePaymentFailedException
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.util.Locale.getDefault

@Component
class StripeRestClient(
    private val stripeProperties: StripeProperties,
    private val objectMapper: ObjectMapper,
) {
    private val restClient: RestClient

    init {
        val requestFactory =
            SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(10000) // 10 seconds
                setReadTimeout(30000) // 30 seconds
            }

        this.restClient =
            RestClient.builder()
                .baseUrl("https://api.stripe.com/v1")
                .defaultHeader("Authorization", "Bearer ${stripeProperties.secretKey}")
                .defaultHeader("Content-Type", "application/x-www-form-urlencoded")
                .requestFactory(requestFactory)
                .build()
    }

    fun createPaymentIntent(
        amount: Long,
        currency: String,
        paymentMethod: String,
    ): String {
        val body =
            listOf(
                "amount=$amount",
                "currency=${currency.lowercase(getDefault())}",
                "payment_method=$paymentMethod",
                "automatic_payment_methods[enabled]=true",
                "automatic_payment_methods[allow_redirects]=never",
            ).joinToString("&")

        return executeStripeCall("/payment_intents", body)
    }

    fun confirmPaymentIntent(paymentIntentId: String): String {
        return executeStripeCall("/payment_intents/$paymentIntentId/confirm", null)
    }

    private fun executeStripeCall(
        uri: String,
        body: String?,
    ): String {
        return try {
            val requestSpec =
                restClient.post()
                    .uri(uri)

            if (body != null) {
                requestSpec
                    .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
            }

            val response =
                requestSpec
                    .retrieve()
                    .toEntity(String::class.java)

            val jsonNode = objectMapper.readTree(response.body)
            jsonNode["id"].asText()
        } catch (e: HttpClientErrorException) {
            // This is a 4xx error (e.g., 400 Bad Request, 402 Payment Required, 404 Not Found)
            // These often contain Stripe's specific error codes (e.g., 'card_declined')
            val errorJson = objectMapper.readTree(e.responseBodyAsString)
            val errorCode = errorJson["error"]?.get("code")?.asText()
            val errorMessage = errorJson["error"]?.get("message")?.asText() ?: e.message

            throw StripePaymentFailedException(
                "Stripe API client error: $errorMessage",
                errorCode,
                e,
            )
        } catch (e: HttpServerErrorException) {
            // This is a 5xx error (problem on Stripe's end)
            val errorJson = objectMapper.readTree(e.responseBodyAsString)
            val errorMessage = errorJson["error"]?.get("message")?.asText() ?: e.message
            throw StripePaymentFailedException(
                "Stripe API server error: $errorMessage",
                null,
                e,
            )
        } catch (e: RestClientException) {
            // This is a lower-level exception (e.g., connection timeout, read timeout, network issue)
            throw StripeConnectionException(
                "Failed to communicate with Stripe: ${e.message}",
                e,
            )
        }
    }
}
