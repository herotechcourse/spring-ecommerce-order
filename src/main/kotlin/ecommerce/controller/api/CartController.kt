package ecommerce.controller.api

import ecommerce.dto.cart.CartAddItemForm
import ecommerce.dto.cart.CartItemResponse
import ecommerce.dto.cart.CartUpdateQuantityForm
import ecommerce.mappers.ProductMapper
import ecommerce.model.Member
import ecommerce.service.CartItemService
import ecommerce.ui.LoginMember
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cart")
class CartController(
    private val cartItemService: CartItemService,
) {
    @GetMapping
    fun viewCart(
        @LoginMember member: Member,
        @RequestParam(defaultValue = "0") pageNumber: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "name") sortBy: String,
    ): ResponseEntity<Page<CartItemResponse>> {
        val memberId = member.id
        val pages = cartItemService.getCartItemsByMemberId(memberId, pageNumber, pageSize, sortBy)
        val alteredPages =
            pages.map { cartItem ->
                CartItemResponse(
                    product = ProductMapper.toResponse(cartItem.product),
                    quantity = cartItem.quantity,
                    createdAt = cartItem.createdAt,
                )
            }

        return ResponseEntity.ok(alteredPages)
    }

    @PostMapping
    fun addToCart(
        @RequestBody @Valid cartForm: CartAddItemForm,
        @LoginMember member: Member,
    ): ResponseEntity<String> {
        val memberId = member.id
        cartItemService.addCartItem(memberId, cartForm.productId, cartForm.quantity)
        return ResponseEntity.ok(MESSAGE_ADD_SUCCESS)
    }

    @PutMapping("/{productId}")
    fun updateQuantity(
        @PathVariable productId: Long,
        @RequestBody @Valid cartForm: CartUpdateQuantityForm,
        @LoginMember member: Member,
    ): ResponseEntity<String> {
        val memberId = member.id
        val message = cartItemService.updateQuantity(memberId, productId, cartForm.quantity)
        return ResponseEntity.ok(message)
    }

    @DeleteMapping("/{productId}")
    fun removeFromCart(
        @PathVariable productId: Long,
        @LoginMember member: Member,
    ): ResponseEntity<String> {
        val memberId = member.id
        val message = cartItemService.removeCartItem(memberId, productId)
        return ResponseEntity.ok(message)
    }

    companion object {
        const val MESSAGE_ADD_SUCCESS = "Item added to cart"
    }
}
