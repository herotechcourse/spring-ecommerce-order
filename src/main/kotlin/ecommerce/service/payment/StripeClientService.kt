package ecommerce.service.payment

import org.springframework.stereotype.Service

@Service
class StripeClientService(
    private val stripeRestClient: StripeRestClient,
) {
    fun createPaymentIntent(
        amount: Long,
        currency: String,
        paymentMethod: String,
    ): String {
        return stripeRestClient.createPaymentIntent(amount, currency, paymentMethod)
    }

    fun confirmPaymentIntent(paymentIntentId: String): String {
        return stripeRestClient.confirmPaymentIntent(paymentIntentId)
    }
}
