package ecommerce.repository

import ecommerce.model.Cart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CartRepository : JpaRepository<Cart, Long> {
    fun findByMemberId(memberId: Long): Cart?

    fun findByIdAndMemberId(
        cartId: Long,
        memberId: Long,
    ): Cart?

    fun deleteByMemberId(memberId: Long)
}
