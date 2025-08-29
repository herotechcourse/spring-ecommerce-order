package ecommerce.controller

import ecommerce.dto.auth.AuthenticatedUser
import ecommerce.dto.order.CreateOrderRequest
import ecommerce.dto.order.OrderResponse
import ecommerce.dto.order.toResponse
import ecommerce.service.OrderService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RequestMapping("/api/orders")
@RestController
class OrderController(
    private val orderService: OrderService,
) {
    @GetMapping("/{id}")
    fun getOrderById(
        @PathVariable id: Long,
        user: AuthenticatedUser,
    ): OrderResponse {
        val order = orderService.getById(id)
        // Security check: User can only view their own orders (unless admin)
        if (order.member.id != user.userId && !user.isAdmin()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Cannot view orders for different member")
        }
        return order.toResponse()
    }

    @GetMapping()
    fun getOrders(
        pageable: Pageable,
        user: AuthenticatedUser,
    ): Page<OrderResponse> {
        if (!user.isAdmin()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Admin privileges required")
        }
        return orderService.findAllOrders(pageable)
    }

    @PostMapping("/place")
    fun placeOrder(
        @Valid @RequestBody request: CreateOrderRequest,
        user: AuthenticatedUser,
    ): ResponseEntity<OrderResponse> {
        val order = orderService.createOrder(request, user.userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(order.toResponse())
    }

    @GetMapping("/my-orders")
    fun getMyOrders(
        pageable: Pageable,
        user: AuthenticatedUser,
    ): ResponseEntity<Page<OrderResponse>> {
        val result = orderService.getOrdersByMember(user.userId, pageable)
        return ResponseEntity.ok(result)
    }

    @DeleteMapping("/{id}")
    fun deleteOrderById(
        @PathVariable id: Long,
        user: AuthenticatedUser,
    ): ResponseEntity<Unit> {
        val order = orderService.getById(id)
        if (order.member.id != user.userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Cannot delete orders for different member")
        }
        orderService.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}
