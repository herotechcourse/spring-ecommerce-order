package ecommerce.controller

import ecommerce.dto.cart.AddToCartRequest
import ecommerce.model.CartItem
import ecommerce.service.CartItemService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/cart/{cartId}/cart-items")
@RestController
class CartItemController(
    private val cartItemService: CartItemService,
) {
    @PostMapping("")
    fun addToCart(
        @PathVariable cartId: Long,
        @RequestBody request: AddToCartRequest,
    ): ResponseEntity<CartItem> {
        val cartItem = cartItemService.addCartItem(request, cartId)
        return ResponseEntity.ok(cartItem)
    }

    @PutMapping("/{itemId}")
    fun updateCartItemForIncrement(
        @PathVariable itemId: Long,
        @PathVariable cartId: Long,
        @RequestBody request: AddToCartRequest,
    ): ResponseEntity<CartItem> {
        val updatedItem = cartItemService.addCartItem(request, cartId)
        return ResponseEntity.ok(updatedItem)
    }

    @DeleteMapping("/{itemId}")
    fun deleteCartItem(
        @PathVariable itemId: Long,
        @PathVariable cartId: Long,
    ): ResponseEntity<Unit> {
        cartItemService.deleteCartItemById(itemId, cartId)
        return ResponseEntity.noContent().build()
    }
}
