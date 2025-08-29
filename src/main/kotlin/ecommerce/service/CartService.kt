package ecommerce.service

import ecommerce.exception.NotFoundException
import ecommerce.model.Cart
import ecommerce.model.CartItem
import ecommerce.repository.CartItemRepository
import ecommerce.repository.CartRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CartService(
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
) {
    @Transactional(readOnly = true)
    fun getCartByUserId(userId: Long): Cart {
        return cartRepository.findByMemberId(userId)
            ?: throw NotFoundException("Cart not found for user $userId")
    }

    @Transactional(readOnly = true)
    fun getCartItemsOfCartByCartId(
        cartId: Long,
        userId: Long,
    ): List<CartItem> {
        cartRepository.findByIdAndMemberId(cartId, userId)
            ?: throw NotFoundException("Cart requested not found")
        return cartItemRepository.findByCartId(cartId)
    }

    fun clearCart(userId: Long) {
        cartRepository.deleteByMemberId(userId)
    }
}
