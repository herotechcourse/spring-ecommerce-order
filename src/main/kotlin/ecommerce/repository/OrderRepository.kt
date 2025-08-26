package ecommerce.repository

import ecommerce.model.Member
import ecommerce.model.Order
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long> {
    fun findAllByMember(member: Member): List<Order>
}
