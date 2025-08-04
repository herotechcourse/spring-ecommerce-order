package ecommerce.controller.api

import ecommerce.dto.ActiveMemberInfo
import ecommerce.dto.TopProductStats
import ecommerce.exception.AuthorizationException
import ecommerce.model.Member
import ecommerce.repository.MemberRepository
import ecommerce.service.CartStatisticsService
import ecommerce.ui.LoginMember
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.jvm.optionals.getOrNull

@RestController
@RequestMapping("/api/admin/cart-stats")
class CartStatsController(
    private val cartStatisticsService: CartStatisticsService,
    private val memberRepository: MemberRepository,
) {
    @GetMapping("/top5-products")
    fun getTop5Products(
        @LoginMember member: Member,
    ): ResponseEntity<List<TopProductStats>> {
        memberRepository.findById(member.id).getOrNull()
            ?: throw AuthorizationException(String.format(MESSAGE_UNAUTHORIZED, member.id))
        val stats = cartStatisticsService.getTop5AddedProductsInLast30Days()
        return ResponseEntity.ok(stats)
    }

    @GetMapping("/active-members")
    fun getActiveMembers(
        @LoginMember member: Member,
    ): ResponseEntity<List<ActiveMemberInfo>> {
        memberRepository.findById(member.id).getOrNull()
            ?: throw AuthorizationException(String.format(MESSAGE_UNAUTHORIZED, member.id))
        val members = cartStatisticsService.getActiveMembersInLast7Days()
        return ResponseEntity.ok(members)
    }

    companion object {
        const val MESSAGE_UNAUTHORIZED = "Unauthorized access from member with ID: %S"
    }
}
