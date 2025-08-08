package ecommerce.repository

import ecommerce.model.CartItem
import ecommerce.model.Member
import ecommerce.model.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.Optional

interface CartItemRepository : JpaRepository<CartItem, Long>, PagingAndSortingRepository<CartItem, Long> {
    fun findByMemberAndProduct(
        member: Member,
        product: Product,
    ): Optional<CartItem>

    fun findAllByMemberOrderByProductNameAsc(
        member: Member,
        pageable: Pageable,
    ): Page<CartItem>

    fun findAllByMemberOrderByProductPriceAsc(
        member: Member,
        pageable: Pageable,
    ): Page<CartItem>
}
