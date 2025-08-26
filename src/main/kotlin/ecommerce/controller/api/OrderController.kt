package ecommerce.controller.api

import ecommerce.dto.order.OrderRequest
import ecommerce.dto.order.OrderResponse
import ecommerce.model.Member
import ecommerce.service.OrderService
import ecommerce.ui.LoginMember
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
) {
    @PostMapping
    fun placeOrder(
        @LoginMember member: Member,
        @RequestBody req: OrderRequest,
    ): ResponseEntity<OrderResponse> {
        val response = orderService.placeOrder(member, req)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun getOrdersForMember(
        @LoginMember member: Member,
    ): ResponseEntity<List<OrderResponse>> {
        val orders = orderService.getOrdersForMember(member)
        return ResponseEntity.ok(orders)
    }
}
