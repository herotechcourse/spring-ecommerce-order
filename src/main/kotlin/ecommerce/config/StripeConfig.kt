package ecommerce.config

import ecommerce.dto.checkout.CheckoutResponse
import ecommerce.dto.payment.PaymentIntentRequest
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class StripeClient(
    private val stripeProperties: StripeProperties,
) {
    private val restClient = RestClient.create()

    fun createCheckoutSession(req: PaymentIntentRequest): CheckoutResponse? {
        val amountInCents = (req.amount * 100).toInt()
        val body =
            listOf(
                "amount=$amountInCents",
                "currency=${req.currency.name}",
                "payment_method=${req.paymentMethod}",
                "confirm=true",
                "automatic_payment_methods[enabled]=true",
                "automatic_payment_methods[allow_redirects]=never",
            ).joinToString("&")

        return try {
            restClient.post()
                .uri("https://api.stripe.com/v1/payment_intents")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ${stripeProperties.secretKey}")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .toEntity(String::class.java)

            CheckoutResponse(
                id = "pi_mock_${System.currentTimeMillis()}",
                client_secret = "pi_mock_${System.currentTimeMillis()}_secret_mock",
                amount = amountInCents,
                currency = req.currency.name.lowercase(),
                status = "requires_confirmation",
                payment_method = req.paymentMethod,
                orderId = 0L,
                orderStatus = "PENDING",
                items = emptyList(),
            )
        } catch (e: Exception) {
            throw IllegalArgumentException("Stripe error: ${e.message}")
        }
    }
}

@ConfigurationProperties("stripe")
data class StripeProperties(
    val secretKey: String = "sk_test_default_key",
)
