package ecommerce.service.payment

import com.stripe.model.PaymentIntent
import com.stripe.param.PaymentIntentCreateParams
import org.springframework.stereotype.Component

@Component
class StripeClientService {
    fun createPaymentIntent(
        amount: Long,
        currency: String,
        paymentMethod: String,
    ): PaymentIntent {
        val params =
            PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .setPaymentMethod(paymentMethod)
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                        .build(),
                )
                .build()

        return PaymentIntent.create(params)
    }

    fun retrievePaymentIntent(id: String): PaymentIntent {
        return PaymentIntent.retrieve(id)
    }

    fun confirmPaymentIntent(id: String) {
        val paymentIntent = retrievePaymentIntent(id)
        paymentIntent.confirm()
    }
}
