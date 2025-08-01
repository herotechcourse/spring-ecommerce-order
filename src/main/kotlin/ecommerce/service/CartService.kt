package ecommerce.service

import ecommerce.dto.CartRequest
import ecommerce.dto.MemberStatsResponse
import ecommerce.dto.ProductStatResponse
import ecommerce.entity.Cart
import ecommerce.entity.Member
import ecommerce.repository.CartJpaRepository
import ecommerce.repository.CartStaticsRepository
import ecommerce.repository.ProductJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class CartService(
    private val cartRepository: CartJpaRepository,
    private val productRepository: ProductJpaRepository,
    private val cartStaticsRepository: CartStaticsRepository,
) {
    fun addToCart(
        member: Member,
        request: CartRequest,
    ) {
        val product = productRepository.findByIdOrNull(request.productId) ?: throw NoSuchElementException()
        cartRepository.save(Cart(member, product))
    }

    fun getCartItems(memberId: Long): List<Cart> {
        return cartRepository.findByMemberId(memberId)
    }

    fun getCartItemsPaginated(
        memberId: Long,
        pageable: Pageable,
    ): Page<Cart> {
        return cartRepository.findByMemberId(memberId, pageable)
    }

    fun removeFromCart(
        memberId: Long,
        productId: Long,
    ) {
        cartRepository.deleteByMemberIdAndProductId(memberId, productId)
    }

    fun getTop5MostAddedProducts(): List<ProductStatResponse> {
        return cartStaticsRepository.getTop5MostAddedProducts()
    }

    fun getRecentlyActiveMembers(): List<MemberStatsResponse> {
        return cartStaticsRepository.getRecentlyActiveMembers()
    }
}
