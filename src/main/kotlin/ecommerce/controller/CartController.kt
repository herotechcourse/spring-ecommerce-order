package ecommerce.controller

import ecommerce.annotations.LoginMember
import ecommerce.dto.CartRequest
import ecommerce.dto.MemberResponse
import ecommerce.entity.CartItemEntity
import ecommerce.service.CartService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/protected/cart")
class CartController(
    private val cartService: CartService,
) {
    @PostMapping
    fun addToCart(
        @RequestBody request: CartRequest,
        @LoginMember member: MemberResponse,
    ): ResponseEntity<Void> {
        cartService.addToCart(member.id, request.productOptionId)

        return ResponseEntity.created(
            URI.create("/api/cart"),
        ).build()
    }

    @DeleteMapping
    fun removeFromCart(
        @RequestBody request: CartRequest,
        @LoginMember member: MemberResponse,
    ): ResponseEntity<Void> {
        cartService.removeFromCart(member.id, request.productOptionId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun getCart(
        @LoginMember member: MemberResponse,
    ): List<CartItemEntity> {
        return cartService.getCartItems(member.id)
    }

    @GetMapping("/?page=1&size=10")
    fun getAllCartItems(
        @PageableDefault(size = 10, sort = ["created_at"]) pageable: Pageable,
    ): Page<CartItemEntity> {
        return cartService.getAllCartItems(
            page = pageable.pageNumber,
            size = pageable.pageSize,
            sortBy = pageable.sort.firstOrNull()?.property ?: "created_at",
            direction = pageable.sort.firstOrNull()?.direction ?: Sort.Direction.ASC,
        )
    }

    @GetMapping("/quantity")
    fun getByQuantity(
        @RequestParam quantity: Int,
        @RequestParam page: Int,
        @RequestParam size: Int,
    ): Page<CartItemEntity> {
        return cartService.getItemsByQuantity(quantity, page, size)
    }
}
