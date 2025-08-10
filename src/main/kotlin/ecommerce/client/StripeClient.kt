package ecommerce.client

import ecommerce.config.StripeProperties
import ecommerce.dto.PaymentRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class StripeClient(
    private val stripeProperties: StripeProperties,
) {
    private val restClient = RestClient.create()

    fun createCheckoutSession(req: PaymentRequest): String? {
        val body = listOf(
            "amount=${req.amount}",
            "currency=${req.currency}",
            "payment_method=${req.paymentMethod}",
            "confirm=true",
            "automatic_payment_methods[enabled]=true",
            "automatic_payment_methods[allow_redirects]=never"
        ).joinToString("&")

        return try {
            val response = restClient.post()
                .uri("https://api.stripe.com/v1/payment_intents")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${stripeProperties.secretKey}")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .toEntity(String::class.java)

            response.body ?: throw IllegalStateException("Empty response from Stripe")
        } catch (e: Exception) {
            throw IllegalArgumentException("Stripe error: ${e.message}")
        }
    }
}