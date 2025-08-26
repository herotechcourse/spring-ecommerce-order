package ecommerce.client

import com.fasterxml.jackson.databind.ObjectMapper
import ecommerce.config.StripeProperties
import ecommerce.dto.payment.StripeErrorResponse
import ecommerce.dto.payment.StripePaymentResponse
import ecommerce.exception.PaymentClientException
import ecommerce.exception.PaymentServerException
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import java.time.Duration

private val logger = KotlinLogging.logger {}

@Component
class StripeClient(
    private val stripeProperties: StripeProperties,
    private val objectMapper: ObjectMapper,
    private val restClient: RestClient = defaultRestClient(),
) {
    companion object {
        private fun defaultRestClient(): RestClient =
            RestClient.builder()
                .requestFactory(
                    SimpleClientHttpRequestFactory().apply {
                        setConnectTimeout(Duration.ofSeconds(3).toMillis().toInt())
                        setReadTimeout(Duration.ofSeconds(10).toMillis().toInt())
                    },
                )
                .build()
    }

    fun createPaymentIntent(
        amount: Long,
        currency: String = "usd",
    ): StripePaymentResponse {
        // Stripe expects application/x-www-form-urlencoded
        val form =
            LinkedMultiValueMap<String, String>().apply {
                add("amount", amount.toString()) // smallest currency unit
                add("currency", currency)
                // Optionally enable automatic payment methods:
                // add("automatic_payment_methods[enabled]", "true")
            }

        return try {
            restClient.post()
                .uri("https://api.stripe.com/v1/payment_intents")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${stripeProperties.secretKey}")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(StripePaymentResponse::class.java)!!
        } catch (e: HttpClientErrorException) {
            // 4xx from Stripe
            val msg = parseStripeMessage(e.responseBodyAsString) ?: "Invalid request to Stripe."
            throw PaymentClientException("Stripe Client Error: $msg")
        } catch (e: HttpServerErrorException) {
            // 5xx from Stripe
            val msg = parseStripeMessage(e.responseBodyAsString) ?: "Stripe service unavailable."
            throw PaymentServerException("Stripe Server Error: $msg")
        } catch (e: ResourceAccessException) {
            // timeouts / I/O issues
            logger.error(e) { "Stripe request failed or timed out" }
            throw PaymentServerException("Could not connect to payment service.")
        }
    }

    private fun parseStripeMessage(body: String?): String? =
        try {
            if (body.isNullOrBlank()) {
                null
            } else {
                objectMapper.readValue(body, StripeErrorResponse::class.java).error.message
            }
        } catch (_: Exception) {
            null
        }
}
