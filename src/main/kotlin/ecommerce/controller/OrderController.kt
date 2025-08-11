package ecommerce.controller

import ecommerce.auth.annotation.LoginMember
import ecommerce.dto.order.OrderResponse
import ecommerce.dto.order.PlaceOrderRequest
import ecommerce.dto.order.PlaceOrderResponse
import ecommerce.entity.Member
import ecommerce.service.OrderQueryService
import ecommerce.service.OrderService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
    private val orderQueryService: OrderQueryService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun placeOrder(
        @LoginMember memberId: Long,
        @RequestBody request: PlaceOrderRequest,
    ): PlaceOrderResponse {
        return orderService.placeOrder(memberId, request)
    }


    @GetMapping
    fun getOrders(
        @LoginMember member: Member,
        pageable: Pageable,
    ): ResponseEntity<Page<OrderResponse>> {
        val orders = orderQueryService.getOrdersForMember(member.id, pageable)
        return ResponseEntity.ok(orders)
    }


    @GetMapping("/{orderId}")
    fun getOrderDetail(
        @LoginMember member: Member,
        @PathVariable orderId: Long,
    ): ResponseEntity<OrderResponse> {
        val order = orderQueryService.getOrderDetail(member.id, orderId)
        return ResponseEntity.ok(order)
    }
}