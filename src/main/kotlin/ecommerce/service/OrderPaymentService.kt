package ecommerce.service

import ecommerce.client.PaymentRequest
import ecommerce.client.PaymentResponse
import ecommerce.client.StripeClient
import ecommerce.exception.PaymentFailedException
import ecommerce.model.Order
import org.springframework.stereotype.Service

@Service
class OrderPaymentService(
    private val stripeClient: StripeClient,
) {
    fun initiatePayment(order: Order): PaymentResponse {
        val paymentMethod = order.paymentMethod ?: throw PaymentFailedException(MESSAGE_PAYMENT_METHOD_REQUIRED)
        val paymentRequest =
            PaymentRequest(
                amount = order.paymentAmount.toInt(),
                currency = order.currency,
                paymentMethod = paymentMethod,
            )
        return stripeClient.createCheckoutSession(paymentRequest)
            ?: throw PaymentFailedException(MESSAGE_PAYMENT_FAILED_WITH_NULL)
    }

    companion object {
        const val MESSAGE_PAYMENT_METHOD_REQUIRED = "Payment method required"
        const val MESSAGE_PAYMENT_FAILED_WITH_NULL = "Payment failed with null"
    }
}
