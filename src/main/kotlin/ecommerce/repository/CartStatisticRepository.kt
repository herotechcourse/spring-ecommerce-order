package ecommerce.repository

import ecommerce.dto.cartStatistics.MembersWhoAddedToCartDTO
import ecommerce.dto.cartStatistics.TopAddedProductsDTO
import ecommerce.enums.CartAction
import ecommerce.model.CartStatistic
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface CartStatisticRepository : JpaRepository<CartStatistic, Long> {
    @Query(
        """
    SELECT new ecommerce.dto.cartStatistics.TopAddedProductsDTO(
        cs.optionName,
        COUNT(cs),
        MAX(cs.createdAt)
    )
    FROM CartStatistic cs
    WHERE cs.action = :action
      AND cs.createdAt >= :since
    GROUP BY cs.optionName
    ORDER BY COUNT(cs) DESC, MAX(cs.createdAt) DESC
    """,
    )
    fun findTopProducts(
        @Param("action") action: CartAction,
        @Param("since") since: LocalDateTime,
        pageable: Pageable,
    ): List<TopAddedProductsDTO>

    @Query(
        """
    SELECT new ecommerce.dto.cartStatistics.MembersWhoAddedToCartDTO(
        cs.userId,
        cs.userName,
        cs.userEmail
    )
    FROM CartStatistic cs
    WHERE cs.createdAt >= :since
    GROUP BY cs.userId, cs.userName, cs.userEmail
    ORDER BY MAX(cs.createdAt) DESC
    """,
    )
    fun findActiveUsersSince(
        @Param("since") since: LocalDateTime,
    ): List<MembersWhoAddedToCartDTO>
}
