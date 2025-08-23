package ecommerce.service.payment

import com.stripe.model.PaymentIntent
import ecommerce.config.StripeProperties
import org.springframework.http.HttpStatusCode
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
    ): PaymentIntent {
        val params =
            mapOf(
                "amount" to amount.toString(),
                "currency" to currency.lowercase(getDefault()),
                "payment_method_types[]" to paymentMethod,
                "confirm" to "true",
            )

        return restClient.post()
            .uri("/payment_intents")
            .body(params)
            .retrieve()
            .onStatus(HttpStatusCode::isError) { request, response ->
                val errorBody = response.body as String
                throw StripeApiException(
                    "Stripe API error: ${response.statusCode} - $errorBody",
                    response.statusCode.value(),
                )
            }
            .body(PaymentIntent::class.java)
            ?: throw StripeApiException("Stripe API returned null response", 500)
    }

    fun confirmPaymentIntent(paymentIntentId: String): PaymentIntent {
        return restClient.post()
            .uri("/payment_intents/$paymentIntentId/confirm")
            .retrieve()
            .onStatus(HttpStatusCode::isError) { request, response ->
                val errorBody = response.body as String
                throw StripeApiException(
                    "Stripe confirmation error: ${response.statusCode} - $errorBody",
                    response.statusCode.value(),
                )
            }
            .body(PaymentIntent::class.java)
            ?: throw StripeApiException("Stripe API returned null response", 500)
    }

    fun retrievePaymentIntent(paymentIntentId: String): PaymentIntent {
        return restClient.get()
            .uri("/payment_intents/$paymentIntentId")
            .retrieve()
            .body(PaymentIntent::class.java)
            ?: throw StripeApiException("Payment intent not found", 404)
    }
}

class StripeApiException(message: String, val statusCode: Int) : RuntimeException(message)
