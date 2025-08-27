package ecommerce.controller

import ecommerce.annotation.LoginMember
import ecommerce.dto.MemberDto
import ecommerce.dto.OrderResponse
import ecommerce.dto.OrderStatusResponse
import ecommerce.dto.stripe.PaymentRequest
import ecommerce.mapper.toResponse
import ecommerce.model.OrderStatus
import ecommerce.service.OrderService
import org.springframework.http.ResponseEntity
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
    @PostMapping("/place")
    fun placeOrder(
        @LoginMember member: MemberDto,
        @RequestBody paymentRequest: PaymentRequest,
    ): ResponseEntity<OrderResponse> {
        val order = orderService.placeOrder(member.id, paymentRequest)
        val response = order.toResponse()

        return if (order.status == OrderStatus.COMPLETED) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.badRequest().body(response)
        }
    }

    @GetMapping("/{orderId}/status")
    fun getOrderStatus(
        @LoginMember member: MemberDto,
        @PathVariable orderId: Long,
    ): ResponseEntity<OrderStatusResponse> {
        val orderStatus = orderService.getOrderStatus(orderId, member.id)
        return ResponseEntity.ok(orderStatus)
    }
}
