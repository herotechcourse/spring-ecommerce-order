package ecommerce.controller.api

import ecommerce.dto.member.ActiveMemberInfo
import ecommerce.dto.product.TopProductStats
import ecommerce.model.Member
import ecommerce.service.CartStatisticsService
import ecommerce.ui.LoginMember
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/cart-stats")
class CartStatsController(
    private val cartStatisticsService: CartStatisticsService,
) {
    @GetMapping("/top5-products")
    fun getTop5Products(
        @LoginMember member: Member,
    ): ResponseEntity<List<TopProductStats>> {
        val stats = cartStatisticsService.getTop5AddedProductsInLast30Days()
        return ResponseEntity.ok(stats)
    }

    @GetMapping("/active-members")
    fun getActiveMembers(
        @LoginMember member: Member,
    ): ResponseEntity<List<ActiveMemberInfo>> {
        val members = cartStatisticsService.getActiveMembersInLast7Days()
        return ResponseEntity.ok(members)
    }
}
