package ecommerce.service

import ecommerce.client.StripeClient
import ecommerce.dto.PaymentRequest
import ecommerce.exception.BadRequestException
import ecommerce.exception.ExternalServiceException
import ecommerce.model.Order
import ecommerce.model.OrderStatus
import ecommerce.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val stripeClient: StripeClient,
    private val orderRepository: OrderRepository,
) {
    @Transactional
    fun createPaymentIntent(req: PaymentRequest): String? {
        try {
            val response = stripeClient.createCheckoutSession(req)
            return response
        } catch (e: BadRequestException) {
            throw e
        } catch (e: ExternalServiceException) {
            throw e
        } catch (e: Exception) {
            // retry??
            throw RuntimeException(e) // this should roll back the transaction
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun processPayment(order: Order) {
        try {
            val sessionId = createPaymentIntent(PaymentRequest(order.paymentAmount, order.currency, order.paymentMethod))
            order.status = OrderStatus.PAID
            order.checkoutSessionId = sessionId
            orderRepository.save(order)
        } catch (e: Exception) {
            order.status = OrderStatus.FAILED
            orderRepository.save(order)
            throw e
        }
    }
}
