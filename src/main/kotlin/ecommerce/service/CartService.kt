package ecommerce.service

import ecommerce.dto.MemberStatsResponse
import ecommerce.dto.ProductStatResponse
import ecommerce.model.CartItem
import ecommerce.repository.CartRepository
import org.springframework.stereotype.Service

@Service
class CartService(
    private val cartRepository: CartRepository,
) {
    fun addToCart(
        memberId: Long,
        productId: Long,
    ) {
        cartRepository.addToCart(memberId, productId)
    }

    fun getCartItems(memberId: Long): List<CartItem> {
        return cartRepository.getCartItems(memberId)
    }

    fun removeFromCart(
        memberId: Long,
        productId: Long,
    ) {
        cartRepository.removeFromCart(memberId, productId)
    }

    fun getTop5MostAddedProducts(): List<ProductStatResponse> {
        return cartRepository.getTop5MostAddedProducts()
    }

    fun getRecentlyActiveMembers(): List<MemberStatsResponse> {
        return cartRepository.getRecentlyActiveMembers()
    }
}
