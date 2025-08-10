package ecommerce.service

import ecommerce.dto.CartRequest
import ecommerce.dto.MemberStatsResponse
import ecommerce.dto.ProductStatResponse
import ecommerce.entity.Cart
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
            cartRepository.findByMemberIdAndId(
                member.id,
                request.cartId,
            ) ?: Cart(member)

        cart.add(option, request.quantity)
        cartRepository.save(cart)
    }

    @Transactional(readOnly = true)
    fun getCartItems(memberId: Long): List<Cart> {
        return cartRepository.findByMemberId(memberId)
    }

    @Transactional
    fun removeOptionFromCart(
        member: Member,
        request: CartRequest,
    ) {
        val cart =
            cartRepository.findByMemberIdAndId(
                member.id,
                request.cartId,
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
}
