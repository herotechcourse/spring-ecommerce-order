package ecommerce.service

import ecommerce.client.StripeClient
import ecommerce.dto.PaymentIntentResponse
import ecommerce.dto.PaymentRequest
import ecommerce.exception.BadRequestException
import ecommerce.exception.ExternalServiceException
import ecommerce.model.Order
import ecommerce.model.OrderStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val stripeClient: StripeClient,
) {
    @Transactional
    fun createPaymentIntent(req: PaymentRequest): PaymentIntentResponse {
        try {
            val response = stripeClient.createCheckoutSession(req)
            return response
        } catch (e: BadRequestException) {
            throw e
        } catch (e: ExternalServiceException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    @Transactional
    fun processPayment(order: Order) {
        try {
            val request = PaymentRequest(order.paymentAmount, order.currency, order.paymentMethod)
            order.checkoutSessionId = createPaymentIntent(request).id
            order.setStatus(OrderStatus.PAID, "Payment successful. Order has been placed")
        } catch (e: Exception) {
            order.setStatus(OrderStatus.FAILED, "Payment failed. Reason: ${e.message}")
        }
    }
}
