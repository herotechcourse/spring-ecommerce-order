package ecommerce.controller

import ecommerce.annotation.LoginMember
import ecommerce.dto.OrderDto
import ecommerce.dto.OrderPlacementRequest
import ecommerce.dto.OrderPlacementResponse
import ecommerce.dto.RegisteredMember
import ecommerce.service.OrderService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService,
) {
    @PostMapping("/place")
    fun placeOrder(
        @Valid @RequestBody req: OrderPlacementRequest,
        @LoginMember member: RegisteredMember,
    ): OrderPlacementResponse {
        val response = orderService.placeOrder(req, member)
        return response
    }

    @GetMapping
    fun getOrders(
        @LoginMember member: RegisteredMember,
    ): List<OrderDto> {
        val orders = orderService.getAllOrdersForMember(member.id)
        return orders
    }
}
