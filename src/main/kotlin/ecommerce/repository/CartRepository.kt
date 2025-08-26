package ecommerce.repository

import ecommerce.model.Cart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CartRepository : JpaRepository<Cart, Long> {
    fun findCartByMemberId(memberId: Long): Cart?

    @Query("select c from Cart c left join fetch c.items where c.member.id = :memberId")
    fun findCartWithItemsByMemberId(memberId: Long): Cart?
}
