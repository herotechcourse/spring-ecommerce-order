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
    fun initiatePayment(
        order: Order,
        paymentMethod: String = "pm_card_visa",
    ): PaymentResponse {
        val paymentRequest =
            PaymentRequest(
                amount = order.paymentAmount.toInt(),
                currency = "usd",
                paymentMethod = paymentMethod,
            )
        return stripeClient.createCheckoutSession(paymentRequest)
            ?: throw PaymentFailedException("Payment failed with null")
    }
}
