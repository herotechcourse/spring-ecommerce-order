package ecommerce.repository

import ecommerce.entity.Cart
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface CartJpaRepository : JpaRepository<Cart, Long> {
    fun findByMemberId(memberId: Long): Cart?

    fun findByMemberId(
        memberId: Long,
        pageable: Pageable,
    ): Page<Cart>
}
