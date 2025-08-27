package ecommerce.client

import com.fasterxml.jackson.databind.ObjectMapper
import ecommerce.config.StripeProperties
import ecommerce.dto.payment.StripeErrorResponse
import ecommerce.dto.payment.StripePaymentIntentRequest
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

private val logger = KotlinLogging.logger {}

@Component
class StripeClient(
    private val stripeProperties: StripeProperties,
    private val objectMapper: ObjectMapper,
    restClient: RestClient? = null,
    private val baseUrl: String = "https://api.stripe.com",
) {
    private val restClient: RestClient = restClient ?: buildDefaultRestClient(stripeProperties)

    private fun buildDefaultRestClient(props: StripeProperties): RestClient =
        RestClient.builder()
            .requestFactory(
                SimpleClientHttpRequestFactory().apply {
                    // Assume props expose millisecond values; adjust if they’re Durations
                    setConnectTimeout(props.connectTimeoutMs.toInt())
                    setReadTimeout(props.readTimeoutMs.toInt())
                },
            )
            .build()

    fun createPaymentIntent(
        amount: Long,
        currency: String = "usd",
    ): StripePaymentResponse {
        val form =
            StripePaymentIntentRequest(
                amount = amount,
                currency = currency,
                confirm = true,
                automaticPaymentMethodsEnabled = true,
                automaticPaymentMethodsAllowRedirects = "never",
            ).toForm()

        return try {
            restClient.post()
                .uri("$baseUrl/v1/payment_intents")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${stripeProperties.secretKey}")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(StripePaymentResponse::class.java)!!
        } catch (e: HttpClientErrorException) {
            // 4xx from Stripe
            val raw = e.responseBodyAsString
            val msg = parseStripeMessage(raw) ?: "Invalid request to Stripe."
            logger.warn { "Stripe 4xx: status=${e.statusCode.value()}, message=$msg, body=$raw" }
            throw PaymentClientException("Stripe Client Error: $msg")
        } catch (e: HttpServerErrorException) {
            // 5xx from Stripe
            val raw = e.responseBodyAsString
            val msg = parseStripeMessage(raw) ?: "Stripe service unavailable."
            logger.error(e) { "Stripe 5xx: status=${e.statusCode.value()}, body=$raw" }
            throw PaymentServerException("Stripe Server Error: $msg")
        } catch (e: ResourceAccessException) {
            // timeouts / I/O issues
            logger.error(e) { "Stripe request failed or timed out" }
            throw PaymentServerException("Could not connect to payment service.")
        }
    }

    private fun StripePaymentIntentRequest.toForm(): LinkedMultiValueMap<String, String> =
        LinkedMultiValueMap<String, String>().apply {
            add("amount", amount.toString())
            add("currency", currency)
            add("confirm", confirm.toString())
            add("automatic_payment_methods[enabled]", automaticPaymentMethodsEnabled.toString())
            add("automatic_payment_methods[allow_redirects]", automaticPaymentMethodsAllowRedirects)
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
