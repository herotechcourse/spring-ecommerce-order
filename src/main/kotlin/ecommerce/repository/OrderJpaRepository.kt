package ecommerce.repository

import ecommerce.entity.Order
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface OrderJpaRepository : JpaRepository<Order, Long> {
    fun findByMemberId(
        memberId: Long,
        pageable: Pageable,
    ): Page<Order>
}
