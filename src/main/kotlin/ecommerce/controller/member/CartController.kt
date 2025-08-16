package ecommerce.controller.member

import ecommerce.annotations.LoginMember
import ecommerce.dto.cartProduct.CartProductResponseDto
import ecommerce.dto.response.MessageResponseDto
import ecommerce.model.User
import ecommerce.service.CartService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/member/cart")
class CartController(
    private val cartService: CartService,
) {
    @GetMapping("")
    fun getCartItems(
        @LoginMember user: User,
    ): ResponseEntity<CartProductResponseDto> {
        val products = cartService.getCartProducts(user)
        return ResponseEntity.ok(products)
    }

    @PostMapping("/{id}")
    fun addProduct(
        @LoginMember user: User,
        @PathVariable("id") optionId: Long,
    ): ResponseEntity<MessageResponseDto> {
        val id = cartService.addProductToCart(user, optionId)
        return ResponseEntity.created(URI.create("/cart/$id")).body(MessageResponseDto("Product added to cart"))
    }

    @DeleteMapping("/{id}")
    fun removeProduct(
        @LoginMember user: User,
        @PathVariable("id") optionId: Long,
    ): ResponseEntity<Void> {
        cartService.removeProductFromCart(user, optionId)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/clear")
    fun clearCart(
        @LoginMember user: User,
    ): ResponseEntity<Void> {
        cartService.clearCart(user)
        return ResponseEntity.noContent().build()
    }
}
