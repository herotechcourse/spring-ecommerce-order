package ecommerce.service

import ecommerce.client.StripeClient
import ecommerce.dto.OrderPlacementResponse
import ecommerce.dto.OrderResponseStatus
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

    @Transactional
    fun processPayment(order: Order): OrderPlacementResponse {
        try {
            val sessionId = createPaymentIntent(PaymentRequest(order.paymentAmount, order.currency, order.paymentMethod))
            order.status = OrderStatus.PAID
            order.checkoutSessionId = sessionId
            return OrderPlacementResponse(
                status = OrderResponseStatus.SUCCESS.name,
                orderId = order.id,
                message = "Payment successful. Order has been placed",
            )
        } catch (e: Exception) {
            order.status = OrderStatus.FAILED
            return OrderPlacementResponse(
                status = OrderResponseStatus.FAILURE.name,
                orderId = order.id,
                message = e.message ?: "Payment failed",
            )
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
