package ecommerce.controller.member

import ecommerce.annotations.LoginMember
import ecommerce.dto.order.OrderResponseDto
import ecommerce.dto.order.PlaceOrderRequestDto
import ecommerce.dto.order.PlaceOrderResponseDto
import ecommerce.model.User
import ecommerce.service.OrderService
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
        @LoginMember user: User,
        @RequestBody request: PlaceOrderRequestDto,
    ): ResponseEntity<PlaceOrderResponseDto> {
        val resp = orderService.placeOrder(user.id, request)
        return ResponseEntity.ok(resp)
    }

    @GetMapping
    fun listOrders(
        @LoginMember user: User,
    ): ResponseEntity<List<OrderResponseDto>> {
        val orders = orderService.listOrders(user.id)
        return ResponseEntity.ok(orders)
    }
}
