package ecommerce.controller

import ecommerce.annotation.LoginMember
import ecommerce.dto.MemberDto
import ecommerce.dto.OrderRequest
import ecommerce.dto.OrderResponse
import ecommerce.service.OderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/payments")
class OrderController(private val oderService: OderService) {
    @PostMapping
    fun createPaymentRequest(
        @RequestBody orderRequest: OrderRequest,
        @LoginMember member: MemberDto,
    ): ResponseEntity<OrderResponse> {
        val order = oderService.processOrder(member.id, orderRequest)
        return ResponseEntity.ok(order)
    }
}
