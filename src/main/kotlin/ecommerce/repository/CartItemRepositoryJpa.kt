package ecommerce.repository

import ecommerce.entity.CartEntity
import ecommerce.entity.CartItemEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface CartItemRepositoryJpa : JpaRepository<CartItemEntity, Long> {
    fun findByCartAndProductId(
        cart: CartEntity,
        productId: Long,
    ): CartItemEntity?

    fun findAllByQuantity(
        quantity: Int,
        pageable: Pageable,
    ): Page<CartItemEntity>

    override fun findAll(pageable: Pageable): Page<CartItemEntity>
}
