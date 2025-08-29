package ecommerce.repository

import ecommerce.model.Order
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun findByMemberId(
        memberId: Long,
        pageable: Pageable,
    ): Page<Order>
}
