package ecommerce.repository

import ecommerce.model.Member
import ecommerce.model.OrderItem
import org.springframework.data.jpa.repository.JpaRepository

interface OrderItemRepository : JpaRepository<OrderItem, Long> {
    fun findAllByMember(member: Member): List<OrderItem>
}
