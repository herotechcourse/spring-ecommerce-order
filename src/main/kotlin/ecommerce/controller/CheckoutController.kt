package ecommerce.controller

import ecommerce.dto.auth.AuthenticatedUser
import ecommerce.dto.checkout.CheckoutResponse
import ecommerce.dto.order.CreateOrderRequest
import ecommerce.dto.order.toResponse
import ecommerce.dto.payment.PaymentIntentRequest
import ecommerce.service.OrderService
import ecommerce.service.PaymentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RequestMapping("/api/checkout")
@RestController
class CheckoutController(
    private val orderService: OrderService,
    private val paymentService: PaymentService,
) {
    @PostMapping
    fun processCheckout(
        @Valid @RequestBody request: CreateOrderRequest,
        user: AuthenticatedUser,
    ): ResponseEntity<CheckoutResponse> {
        val order = orderService.createOrder(request, user.userId)

        val paymentIntentRequest =
            PaymentIntentRequest(
                amount = order.totalAmount,
                currency = order.currency,
                paymentMethod = request.paymentMethod,
            )

        val checkoutResponse = paymentService.processPayment(paymentIntentRequest, order)
        orderService.save(order)
        return ResponseEntity.ok(checkoutResponse)
    }

    @PostMapping("/confirm/{orderId}")
    fun confirmCheckout(
        @PathVariable orderId: Long,
        user: AuthenticatedUser,
    ): ResponseEntity<CheckoutResponse> {
        val order = orderService.getById(orderId)

        if (order.member.id != user.userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Order does not belong to member")
        }

        orderService.confirmOrderPayment(orderId)

        val updatedOrder = orderService.getById(orderId)
        val response =
            CheckoutResponse(
                id = "pi_confirmed_${updatedOrder.id}",
                client_secret = null,
                amount = (updatedOrder.totalAmount * 100).toInt(),
                currency = updatedOrder.currency.name.lowercase(),
                status = "succeeded",
                payment_method = null,
                orderId = updatedOrder.id ?: 0L,
                orderStatus = updatedOrder.orderStatus.name,
                items = updatedOrder.toResponse().orderItems,
            )

        return ResponseEntity.ok(response)
    }
}
