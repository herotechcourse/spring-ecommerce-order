package ecommerce.service

import ecommerce.client.PaymentRequest
import ecommerce.client.PaymentResponse
import ecommerce.client.StripeClient
import ecommerce.exception.PaymentFailedException
import org.springframework.stereotype.Service

@Service
class OrderPaymentService(
    private val stripeClient: StripeClient,
) {
    fun initiatePayment(request: PaymentRequest): PaymentResponse {
        return stripeClient.createCheckoutSession(request)
            ?: throw PaymentFailedException(MESSAGE_PAYMENT_FAILED_WITH_NULL)
    }

    companion object {
        const val MESSAGE_PAYMENT_FAILED_WITH_NULL = "Payment failed with null"
    }
}
