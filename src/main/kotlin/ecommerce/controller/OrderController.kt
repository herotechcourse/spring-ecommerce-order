package ecommerce.controller

import ecommerce.annotations.LoginMember
import ecommerce.dto.MemberResponse
import ecommerce.service.OrderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController(private val orderService: OrderService) {
    @PostMapping
    fun createOrder(
        @LoginMember member: MemberResponse,
        @RequestParam productOptionId: Long,
        @RequestParam amount: Int,
    ): ResponseEntity<Void> {
        orderService.createOrder(member.id, productOptionId, amount)
        return ResponseEntity.ok().build()
    }
}
