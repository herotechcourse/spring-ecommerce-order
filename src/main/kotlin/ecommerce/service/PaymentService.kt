package ecommerce.service

import ecommerce.config.StripeClient
import ecommerce.dto.checkout.CheckoutResponse
import ecommerce.dto.order.OrderItemResponse
import ecommerce.dto.payment.PaymentIntentRequest
import ecommerce.exception.FailedPaymentException
import ecommerce.model.Order
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val stripeClient: StripeClient,
) {
    @Transactional
    fun processPayment(
        request: PaymentIntentRequest,
        order: Order,
    ): CheckoutResponse {
        val checkoutResponse =
            stripeClient.createCheckoutSession(request)
                ?: throw FailedPaymentException("Failed to create Stripe checkout session")

        order.stripeCheckoutSessionId = checkoutResponse.id

        updateCheckoutResponseWithOrder(checkoutResponse, order)

        return checkoutResponse
    }

    private fun updateCheckoutResponseWithOrder(
        checkoutResponse: CheckoutResponse,
        order: Order,
    ) {
        checkoutResponse.orderId = order.id ?: 0L
        checkoutResponse.orderStatus = order.orderStatus.name
        checkoutResponse.amount = (order.totalAmount * 100).toInt() // Stripe expects cents
        checkoutResponse.currency = order.currency.name.lowercase()
        checkoutResponse.items =
            order.orderItems.map { orderItem ->
                OrderItemResponse(
                    id = orderItem.id ?: 0L,
                    productName = orderItem.productOption.product.name,
                    optionName = orderItem.productOption.name,
                    quantity = orderItem.quantity,
                    unitPrice = orderItem.unitPrice,
                    totalPrice = orderItem.getTotalAmount(),
                )
            }
    }
}
