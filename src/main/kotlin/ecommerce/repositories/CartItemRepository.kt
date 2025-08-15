package ecommerce.repositories

import ecommerce.dto.ActiveMemberDTO
import ecommerce.dto.TopProductDTO
import ecommerce.entities.CartItemEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CartItemRepository : JpaRepository<CartItemEntity, Long> {
    fun findByMemberId(memberId: Long): List<CartItemEntity>

    fun existsByOptionIdAndMemberId(
        optionId: Long,
        memberId: Long,
    ): Boolean

    fun findByOptionIdAndMemberId(
        optionId: Long,
        memberId: Long,
    ): CartItemEntity?

    fun deleteByOptionIdAndMemberId(
        optionId: Long,
        memberId: Long,
    )

    @Query(
        value = """
    SELECT o.name AS name,
           COUNT(*) AS count,
           MAX(c.added_at) AS mostRecentAddedAt
    FROM cart_item c
    JOIN "option" o ON c.option_id = o.id
    WHERE c.added_at >= DATEADD('DAY', -30, CURRENT_TIMESTAMP)
    GROUP BY c.option_id, o.name
    ORDER BY count DESC, mostRecentAddedAt DESC
    LIMIT 5
  """,
        nativeQuery = true,
    )
    fun findTop5ProductsAddedInLast30Days(): List<TopProductDTO>

    @Query(
        nativeQuery = true,
        value = """
        SELECT DISTINCT m.id, m.name, m.email
        FROM cart_item c
        JOIN member m ON c.member_id = m.id
        WHERE c.added_at >= DATEADD('DAY', -7, CURRENT_TIMESTAMP)
    """,
    )
    fun findDistinctMembersWithCartActivityInLast7Days(): List<ActiveMemberDTO>
}
