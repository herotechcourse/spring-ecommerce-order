package ecommerce.repository

import ecommerce.model.CartItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CartItemRepository : JpaRepository<CartItem, Long> {
    fun findByCartId(cartId: Long): List<CartItem>

    fun findByCartIdAndProductOptionId(
        cartId: Long,
        productOptionId: Long,
    ): List<CartItem>

    fun deleteByCartId(cartId: Long)
}
