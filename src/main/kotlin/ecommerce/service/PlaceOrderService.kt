package ecommerce.service

import ecommerce.dto.PlaceOrderRequest
import ecommerce.dto.PlaceOrderResponse
import org.springframework.stereotype.Service

@Service
class PlaceOrderService(
    private val orderCreationService: OrderCreationService,
    private val paymentService: PaymentService,
    private val paymentFinalizationService: PaymentFinalizationService,
    private val cartService: CartService,
) {
    fun placeOrder(
        memberId: Long,
        req: PlaceOrderRequest,
    ): PlaceOrderResponse {
        val (order, attempt) = orderCreationService.createPending(memberId, req)

        val response = paymentService.pay(req.paymentRequest)

        if (response != null) {
            cartService.clearCart(memberId)
            return paymentFinalizationService.finalizePaid(
                order.id,
                attempt.id,
            )
        }
        return paymentFinalizationService.finalizeFailed(
            order.id,
            attempt.id,
            response,
        )
    }
}
