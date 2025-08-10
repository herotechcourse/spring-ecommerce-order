package ecommerce.repository

import ecommerce.entity.CartEntity
import org.springframework.data.jpa.repository.JpaRepository

interface CartRepositoryJpa : JpaRepository<CartEntity, Long> {
    fun findByMemberId(memberId: Long): CartEntity?
}
