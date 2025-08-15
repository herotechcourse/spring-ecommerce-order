package ecommerce.repository

import ecommerce.model.MemberOrder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MemberOrderRepository : JpaRepository<MemberOrder, Long> {
    fun findAllByUserId(userId: Long): List<MemberOrder>
}
