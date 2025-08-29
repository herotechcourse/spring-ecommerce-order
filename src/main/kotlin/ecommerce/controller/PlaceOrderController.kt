package ecommerce.controller

import ecommerce.auth.annotation.LoginMember
import ecommerce.dto.AuthenticatedMember
import ecommerce.dto.PlaceOrderRequest
import ecommerce.dto.PlaceOrderResponse
import ecommerce.service.PlaceOrderService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/place-order")
class PlaceOrderController(
    private val placeOrderService: PlaceOrderService,
) {
    @PostMapping
    fun placeOrder(
        @LoginMember member: AuthenticatedMember,
        @RequestBody @Valid request: PlaceOrderRequest,
    ): ResponseEntity<PlaceOrderResponse> {
        return ResponseEntity.ok(placeOrderService.placeOrder(member.id, request))
    }
}
