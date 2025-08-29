package ecommerce.repository

import ecommerce.dto.analytics.ActiveUserAnalytics
import ecommerce.dto.analytics.TopProductAnalytics
import ecommerce.model.CartStatistics
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
interface CartStatisticsRepository : JpaRepository<CartStatistics, Long> {
    @Query(
        """
        SELECT new ecommerce.dto.analytics.TopProductAnalytics(
            cs.productOption.product.name,
            CAST(SUM(cs.quantity) AS int),
            MAX(cs.cartItem.createdAt)
        )
        FROM CartStatistics cs
        WHERE cs.cartItem.updatedAt >= :thirtyDaysAgo
        GROUP BY cs.productOption.product.id, cs.productOption.product.name
        ORDER BY SUM(cs.quantity) DESC, MAX(cs.cartItem.createdAt) DESC
    """,
    )
    fun findTop5MostAddedProductsLast30Days(thirtyDaysAgo: LocalDateTime = LocalDateTime.now().minusDays(30)): List<TopProductAnalytics>

    @Query(
        """
        SELECT DISTINCT new ecommerce.dto.analytics.ActiveUserAnalytics(
            cs.cart.member.id,
            cs.cart.member.name,
            cs.cart.member.email
        )
        FROM CartStatistics cs
        WHERE cs.cartItem.updatedAt >= :sevenDaysAgo
    """,
    )
    fun findMembersActiveInLast7Days(sevenDaysAgo: LocalDateTime = LocalDateTime.now().minusDays(7)): List<ActiveUserAnalytics>

    @Modifying
    @Transactional
    @Query("DELETE FROM CartStatistics cs WHERE cs.cart.id = :cartId")
    fun deleteByCartId(cartId: Long)
}
