package ecommerce.repository

import ecommerce.entity.Cart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CartJpaRepository : JpaRepository<Cart, Long> {

    fun findByMemberId(memberId: Long): List<Cart>

    fun deleteByMemberIdAndProductId(memberId: Long, productId: Long)
}
