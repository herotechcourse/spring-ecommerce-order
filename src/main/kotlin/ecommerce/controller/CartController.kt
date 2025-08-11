package ecommerce.controller

import ecommerce.auth.annotation.LoginMember
import ecommerce.dto.CartRequest
import ecommerce.entity.Cart
import ecommerce.entity.Member
import ecommerce.service.CartService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cart")
class CartController(
    private val cartService: CartService,
) {
    @PostMapping
    fun addToCart(
        @RequestBody request: CartRequest,
        @LoginMember member: Member,
    ): ResponseEntity<Unit> {
        cartService.addToCart(member, request)
        return ResponseEntity.ok().build()
    }

    @GetMapping
    fun getCart(
        @LoginMember member: Member,
    ): ResponseEntity<List<Cart>> {
        val cartItems = cartService.getCartItems(member.id)
        return ResponseEntity.ok(cartItems)
    }

    @DeleteMapping("/{optionId}")
    fun removeFromCart(
        @PathVariable optionId: Long,
        @LoginMember member: Member,
    ): ResponseEntity<Unit> {
        cartService.removeFromCart(member.id, optionId)
        return ResponseEntity.noContent().build()
    }
}
