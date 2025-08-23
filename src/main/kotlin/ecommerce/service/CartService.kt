package ecommerce.service

import ecommerce.dto.CartRequest
import ecommerce.dto.MemberStatsResponse
import ecommerce.dto.ProductStatResponse
import ecommerce.entity.Cart
import ecommerce.entity.CartItem
import ecommerce.entity.Member
import ecommerce.repository.CartJpaRepository
import ecommerce.repository.CartStaticsRepository
import ecommerce.repository.OptionJpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CartService(
    private val cartRepository: CartJpaRepository,
    private val cartStaticsRepository: CartStaticsRepository,
    private val optionRepository: OptionJpaRepository,
) {
    @Transactional
    fun addOptionToCart(
        member: Member,
        request: CartRequest,
    ) {
        val option =
            optionRepository.findByProductIdAndId(
                request.productId,
                request.optionId,
            ) ?: throw NoSuchElementException()

        val cart =
            cartRepository.findByMemberId(
                member.id,
            ) ?: Cart(member)

        cart.add(option, request.quantity)
        cartRepository.save(cart)
    }

    @Transactional(readOnly = true)
    fun getCartItems(memberId: Long): List<CartItem> {
        return cartRepository.findByMemberId(memberId)
            ?.items
            ?.toList()
            ?: throw NoSuchElementException("Cart for member $memberId not found")
    }

    @Transactional(readOnly = true)
    fun findCartByIdAndMemberId(
        cartId: Long,
        memberId: Long,
    ): Cart? {
        return cartRepository.findByMemberId(memberId) ?: throw NoSuchElementException()
    }

    @Transactional
    fun removeOptionFromCart(
        member: Member,
        request: CartRequest,
    ) {
        val cart =
            cartRepository.findByMemberId(
                member.id,
            ) ?: throw NoSuchElementException()

        cart.remove(request.optionId, request.quantity)
    }

    @Transactional(readOnly = true)
    fun getTop5MostAddedProducts(): List<ProductStatResponse> {
        return cartStaticsRepository.getTop5MostAddedProducts()
    }

    @Transactional(readOnly = true)
    fun getRecentlyActiveMembers(): List<MemberStatsResponse> {
        return cartStaticsRepository.getRecentlyActiveMembers()
    }

    @Transactional
    fun clearCart(member: Member) {
        val cart =
            cartRepository.findByMemberId(member.id)
                ?: throw NoSuchElementException("Cart for member ${member.id} not found")

        cart.clear()
    }
}
