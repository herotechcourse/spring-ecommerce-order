package ecommerce.controller.member

import ecommerce.dto.order.OrderIntentResponse
import ecommerce.dto.order.OrderListResponse
import ecommerce.dto.order.OrderResponse
import ecommerce.dto.response.MessageResponse
import ecommerce.model.User
import ecommerce.service.MemberOrderService
import ecommerce.utils.annotations.LoginMember
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/member/order")
class MemberOrderController(
    private val orderService: MemberOrderService,
) {
    @GetMapping("")
    fun getAllOrders(
        @LoginMember user: User,
    ): ResponseEntity<OrderListResponse> {
        val response = OrderListResponse(orderService.getUserOrders(user.id))
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{orderId}")
    fun getOrderById(
        @PathVariable orderId: Long,
    ): ResponseEntity<OrderResponse> {
        return ResponseEntity.ok(orderService.getOrderById(orderId))
    }

    @PostMapping("/cart-checkout")
    fun createCheckoutCartIntent(
        @LoginMember user: User,
    ): ResponseEntity<OrderIntentResponse> {
        val response = orderService.createCheckoutCartIntent(user.id)
        val uri = URI.create("/order/checkout/${response.orderId}")
        return ResponseEntity.created(uri).body(response)
    }

    @PostMapping("/confirm-checkout/{orderId}")
    fun confirmCheckout(
        @PathVariable orderId: Long,
    ): ResponseEntity<MessageResponse> {
        orderService.confirmCheckout(orderId)
        return ResponseEntity.ok(MessageResponse("Order confirmed"))
    }
}
