package ecommerce.service.payment

import com.fasterxml.jackson.databind.ObjectMapper
import ecommerce.config.StripeProperties
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.util.Locale.getDefault

@Component
class StripeRestClient(
    private val stripeProperties: StripeProperties,
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

        return try {
            val response =
                restClient.post()
                    .uri("/payment_intents")
                    .body(body)
                    .retrieve()
                    .toEntity(String::class.java)

            val mapper = ObjectMapper()
            val jsonNode = mapper.readTree(response.body)
            val id = jsonNode["id"].asText()

            id
        } catch (e: Exception) {
            throw IllegalArgumentException("Stripe error: ${e.message}")
        }
    }

    fun confirmPaymentIntent(paymentIntentId: String): String {
        return try {
            val response =
                restClient.post()
                    .uri("/payment_intents/$paymentIntentId/confirm")
                    .retrieve()
                    .toEntity(String::class.java)

            val mapper = ObjectMapper()
            val jsonNode = mapper.readTree(response.body)
            val id = jsonNode["id"].asText()

            id
        } catch (e: Exception) {
            throw IllegalArgumentException("Stripe error: ${e.message}")
        }
    }
}
