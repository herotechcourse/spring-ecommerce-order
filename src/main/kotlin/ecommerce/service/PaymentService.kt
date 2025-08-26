package ecommerce.service

import ecommerce.dto.payment.PaymentRequest
import ecommerce.dto.payment.PaymentResponse
import ecommerce.model.Currency
import ecommerce.stripe.StripeClient
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val stripeClient: StripeClient,
) {
    fun processPayment(
        amount: Double,
        currencyCode: String,
        paymentMethod: String,
    ): PaymentResponse {
        val currency = Currency.fromCode(currencyCode)

        val request =
            PaymentRequest(
                amount = amount,
                currency = currency.code,
                paymentMethod = paymentMethod,
            )

        return stripeClient.createCheckoutSession(request)
            ?: throw IllegalStateException("Stripe returned an empty response.")
    }
}
