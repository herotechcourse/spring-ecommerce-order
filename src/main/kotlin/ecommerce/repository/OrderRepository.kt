package ecommerce.repository

import ecommerce.model.Order
import ecommerce.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun findAllByUserOrderByCreatedAtDesc(user: User): List<Order>
}
