package ecommerce.service

import ecommerce.client.StripeClient
import ecommerce.dto.PaymentIntentResponse
import ecommerce.dto.PaymentRequest
import ecommerce.exception.BadRequestException
import ecommerce.exception.ExternalServiceException
import ecommerce.model.Order
import ecommerce.model.OrderStatus
import ecommerce.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val stripeClient: StripeClient,
    private val orderRepository: OrderRepository,
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
            // TODO: retry payment??
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
            order.setStatus(OrderStatus.FAILED, "Payment failed. Reason: ${e.message?.take(100)}")
            // TODO: remove take(100) and find a way to display a nice error message coming from Stripe
        }
    }

//    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = [Exception::class])
//    fun processPayment(order: Order) {
//        try {
//            val sessionId = createPaymentIntent(PaymentRequest(order.paymentAmount, order.currency, order.paymentMethod))
//            order.status = OrderStatus.PAID
//            order.checkoutSessionId = sessionId
//            orderRepository.save(order)
//        } catch (e: Exception) {
//            order.status = OrderStatus.FAILED
//            orderRepository.save(order)
//            throw e
//        }
//    }
}
