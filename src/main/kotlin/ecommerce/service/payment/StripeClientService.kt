package ecommerce.service.payment

import com.stripe.model.PaymentIntent
import com.stripe.param.PaymentIntentCreateParams
import org.springframework.stereotype.Service

@Service
class StripeClientService {
    fun createPaymentIntent(
        amount: Long,
        currency: String,
    ): PaymentIntent {
        val params =
            PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .build()
        return PaymentIntent.create(params)
    }

    fun retrievePaymentIntent(id: String): PaymentIntent {
        return PaymentIntent.retrieve(id)
    }
}
