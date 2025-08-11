package ecommerce.controller.api

import ecommerce.dto.OrderItemResponse
import ecommerce.dto.OrderPlaceForm
import ecommerce.dto.OrderResponse
import ecommerce.exception.EmptyCartException
import ecommerce.exception.InsufficientStockException
import ecommerce.exception.PaymentFailedException
import ecommerce.model.Member
import ecommerce.model.OrderItem
import ecommerce.service.OrderService
import ecommerce.ui.LoginMember
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
) {
    @GetMapping
    fun getOrdersByMemberId(
        @LoginMember member: Member,
    ): ResponseEntity<List<OrderResponse>> {
        val orders = orderService.readOrders(member.id)
        return ResponseEntity.ok(orders)
    }

    @GetMapping("/{orderId}")
    fun getOrderById(
        @PathVariable orderId: Long,
        @LoginMember member: Member,
    ): ResponseEntity<OrderResponse> {
        val order = orderService.readOrder(orderId, member.id)
        return ResponseEntity.ok(order)
    }

    @PostMapping("/place")
    fun placeOrder(
        @Valid @RequestBody orderForm: OrderPlaceForm,
        @LoginMember member: Member,
    ): ResponseEntity<List<OrderItemResponse>> {
        val order = orderService.placeOrder(member.id, orderForm)
        val orderItemResponses = order.orderItems.map { OrderItem.to(it) }
        return ResponseEntity.ok(orderItemResponses)
    }

    @ExceptionHandler(EmptyCartException::class)
    fun handleEmptyCartException(e: Exception): ResponseEntity<Map<String, Any>> {
        val error = mapOf("cart" to e.message)
        val errorBody = mapOf("errors" to error)
        println("EmptyCartException occurred: $errorBody")
        return ResponseEntity.badRequest().body(errorBody)
    }

    @ExceptionHandler(InsufficientStockException::class)
    fun handleInsufficientStockException(e: Exception): ResponseEntity<Map<String, Any>> {
        val error = mapOf("stock" to e.message)
        val errorBody = mapOf("errors" to error)
        println("InsufficientStockException occurred: $errorBody")
        return ResponseEntity.badRequest().body(errorBody)
    }

    @ExceptionHandler(PaymentFailedException::class)
    fun handlePaymentFailedException(e: Exception): ResponseEntity<Map<String, Any>> {
        val error = mapOf("payment" to e.message)
        val errorBody = mapOf("errors" to error)
        println("PaymentFailedException occurred: $errorBody")
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(errorBody)
    }
}
