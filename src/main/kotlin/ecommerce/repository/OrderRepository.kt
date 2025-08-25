package ecommerce.repository

import ecommerce.entity.Order
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long> {
    fun findByIdAndMemberId(
        orderId: Long,
        memberId: Long,
    ): Order?
}
