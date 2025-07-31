package ecommerce.controller

import ecommerce.auth.annotation.LoginMember
import ecommerce.dto.CartRequest
import ecommerce.model.CartItem
import ecommerce.model.Member
import ecommerce.service.CartService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
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
        cartService.addToCart(member.id, request.productId)
        return ResponseEntity.ok().build()
    }

    @GetMapping
    fun getCart(
        @LoginMember member: Member,
    ): ResponseEntity<List<CartItem>> {
        val cartItems = cartService.getCartItems(member.id)
        return ResponseEntity.ok(cartItems)
    }

    @DeleteMapping
    fun removeFromCart(
        @RequestBody request: CartRequest,
        @LoginMember member: Member,
    ): ResponseEntity<Unit> {
        cartService.removeFromCart(member.id, request.productId)
        return ResponseEntity.noContent().build()
    }
}
