package ecommerce.controller

import ecommerce.auth.annotation.LoginMember
import ecommerce.dto.MemberStatsResponse
import ecommerce.dto.ProductStatResponse
import ecommerce.model.Member
import ecommerce.service.CartService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/stats")
class CartAdminController(
    private val cartService: CartService,
) {
    @GetMapping("/top-products")
    fun getTopProducts(
        @LoginMember member: Member,
    ): ResponseEntity<List<ProductStatResponse>> {
        val result = cartService.getTop5MostAddedProducts()
        return ResponseEntity.ok(result)
    }

    @GetMapping("/active-members")
    fun getActiveMembers(
        @LoginMember member: Member,
    ): ResponseEntity<List<MemberStatsResponse>> {
        val result = cartService.getRecentlyActiveMembers()
        return ResponseEntity.ok(result)
    }
}
