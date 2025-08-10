package ecommerce.client

import ecommerce.config.StripeProperties
import ecommerce.dto.payment.StripePaymentResponse
import ecommerce.exception.PaymentClientException
import ecommerce.exception.PaymentServerException
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.net.SocketTimeoutException

@Component
class StripeClient(
    private val stripeProperties: StripeProperties,
) {
    private val restClient = RestClient.create()

    fun createPaymentIntent(
        amount: Long,
        currency: String = "usd",
    ): StripePaymentResponse {
        val body =
            listOf(
                "amount=$amount",
                "currency=$currency",
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
                .onStatus({ it.is4xxClientError }) { _, _ ->
                    throw PaymentClientException("Invalid request to Stripe")
                }
                .onStatus({ it.is5xxServerError }) { _, _ ->
                    throw PaymentServerException("Stripe server error")
                }
                .body(StripePaymentResponse::class.java)!!
        } catch (e: SocketTimeoutException) {
            throw PaymentServerException("Stripe request timed out") as Throwable
        }
    }
}
