package ecommerce.controller

import ecommerce.auth.annotation.LoginMember
import ecommerce.dto.order.PlaceOrderRequest
import ecommerce.dto.order.PlaceOrderResponse
import ecommerce.service.OrderService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/orders")
class OrderController (
    private val orderService: OrderService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun placeOrder(
        @LoginMember memberId: Long,
        @RequestBody request: PlaceOrderRequest
    ): PlaceOrderResponse {
        return orderService.placeOrder(memberId, request)
    }
}