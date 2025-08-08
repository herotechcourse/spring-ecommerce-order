package ecommerce.controller.api

import ecommerce.dto.ActiveMemberInfo
import ecommerce.dto.TopProductStats
import ecommerce.service.CartStatisticsService
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
    fun getTop5Products(): ResponseEntity<List<TopProductStats>> {
        val stats = cartStatisticsService.getTop5AddedProductsInLast30Days()
        return ResponseEntity.ok(stats)
    }

    @GetMapping("/active-members")
    fun getActiveMembers(): ResponseEntity<List<ActiveMemberInfo>> {
        val members = cartStatisticsService.getActiveMembersInLast7Days()
        return ResponseEntity.ok(members)
    }
}
