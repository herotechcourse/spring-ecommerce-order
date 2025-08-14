package ecommerce.repository

import ecommerce.dto.ActiveUsersResponse
import ecommerce.dto.TopProductStats
import ecommerce.model.CartHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface CartHistoryJpaRepository : JpaRepository<CartHistory, Long> {
    @Query(
        """
        SELECT new ecommerce.dto.TopProductStats(
        ch.option.product.name,
        COUNT(ch.option.product.name),
        MAX(ch.createdAt)
    )
    FROM CartHistory ch
    WHERE ch.createdAt >= :since
    GROUP BY ch.option.product.name
    ORDER BY COUNT(ch.option.product.name) DESC, MAX(ch.createdAt) DESC
    """,
    )
    fun getTopProducts(
        @Param("since") since: LocalDateTime,
    ): List<TopProductStats>

    @Query(
        """
    SELECT DISTINCT new ecommerce.dto.ActiveUsersResponse(
        ch.member.id, ch.member.name, ch.member.email
    )
    FROM CartHistory ch
    WHERE ch.createdAt > :since
    ORDER BY ch.member.id
    """,
    )
    fun getTop5ActiveUsers(
        @Param("since") since: LocalDateTime,
    ): List<ActiveUsersResponse>
}
