package ecommerce.controller

import ecommerce.annotations.LoginMember
import ecommerce.dto.CreateOrderRequest
import ecommerce.dto.CreateOrderResponse
import ecommerce.dto.MemberResponse
import ecommerce.service.OrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController(private val orderService: OrderService) {
    @PostMapping
    fun createOrder(
        @LoginMember member: MemberResponse,
        @RequestBody body: CreateOrderRequest,
    ): ResponseEntity<CreateOrderResponse> {
        val response =
            orderService.createOrder(member.id, body.productOptionId, body.quantity, body.paymentMethod, body.currency)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{id}/process-payment")
    fun confirmPayment(
        @PathVariable id: Long,
        @LoginMember member: MemberResponse,
    ): ResponseEntity<String> {
        orderService.confirmPayment(id, member)
        return ResponseEntity.ok("Payment successfully fulfilled")
    }
}
