package ecommerce.service

import ecommerce.dto.PaymentRequest
import ecommerce.dto.PaymentResponse
import ecommerce.stripe.StripeClient
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val stripe: StripeClient,
) {
    fun pay(paymentRequest: PaymentRequest): PaymentResponse? {
        return stripe.createCheckoutSession(paymentRequest)
    }
}
