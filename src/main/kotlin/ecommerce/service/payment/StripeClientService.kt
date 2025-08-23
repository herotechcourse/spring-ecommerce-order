package ecommerce.service.payment

import ecommerce.config.StripeProperties
import org.springframework.stereotype.Service

@Service
class StripeClientService(
    private val stripeProperties: StripeProperties,
) {
    fun createPaymentIntent(
        amount: Long,
        currency: String,
        paymentMethod: String,
    ): String {
        return StripeRestClient(stripeProperties).createPaymentIntent(amount, currency, paymentMethod)
    }

    fun confirmPaymentIntent(paymentIntentId: String): String {
        return StripeRestClient(stripeProperties).confirmPaymentIntent(paymentIntentId)
    }
}
