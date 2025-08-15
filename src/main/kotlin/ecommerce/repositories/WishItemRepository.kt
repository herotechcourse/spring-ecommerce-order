package ecommerce.repositories

import ecommerce.entities.WishItemEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface WishItemRepository : JpaRepository<WishItemEntity, Long> {
    fun findByMemberId(memberId: Long): List<WishItemEntity>

    fun findByMemberId(
        memberId: Long,
        page: Pageable,
    ): Page<WishItemEntity>

    fun findByProductIdAndMemberId(
        productId: Long,
        memberId: Long,
    ): WishItemEntity?

    fun deleteByProductIdAndMemberId(
        productId: Long,
        memberId: Long,
    )
}
