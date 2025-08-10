package ecommerce.service

import ecommerce.dto.order.PlaceOrderRequest
import ecommerce.dto.order.PlaceOrderResponse
import ecommerce.client.StripeClient
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val stripeClient: StripeClient
) {

    fun placeOrder(memberId: Long, request: PlaceOrderRequest): PlaceOrderResponse {
        // TODO: validate stock, member, quantity

        // TODO: call stripeClient.createPaymentIntent(...)
        // TODO: handle success vs decline
        // TODO: persist order/payment in Step 2.2

        return PlaceOrderResponse(
            orderStatus = "PENDING",
            paymentIntentId = null,
            amount = null,
            currency = null,
            items = emptyList()
        )
    }
}