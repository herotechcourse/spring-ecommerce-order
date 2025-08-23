package ecommerce.service.payment

import com.stripe.model.PaymentIntent
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
    ): PaymentIntent {
        return StripeRestClient(stripeProperties).createPaymentIntent(amount, currency, paymentMethod)
    }

    fun confirmPaymentIntent(paymentIntentId: String): PaymentIntent {
        return StripeRestClient(stripeProperties).confirmPaymentIntent(paymentIntentId)
    }

    fun retrievePaymentIntent(paymentIntentId: String): PaymentIntent {
        return StripeRestClient(stripeProperties).retrievePaymentIntent(paymentIntentId)
    }
}
