package ecommerce.controller.order

import ecommerce.annotation.LoginMember
import ecommerce.controller.order.usecase.OrderCreationUseCase
import ecommerce.dto.MemberLoginDTO
import ecommerce.dto.OrderDTO
import ecommerce.model.PaymentRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/order")
class OrderController(private val orderCreationUseCase: OrderCreationUseCase) {
    @PostMapping
    fun createOrder(
        @LoginMember member: MemberLoginDTO,
        @RequestBody paymentRequest: PaymentRequest,
    ): ResponseEntity<OrderDTO> {
        val order = orderCreationUseCase.create(member, paymentRequest)
        return ResponseEntity.ok(order)
    }
}
