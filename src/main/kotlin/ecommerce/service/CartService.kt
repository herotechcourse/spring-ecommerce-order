package ecommerce.service

import ecommerce.dto.CartRequest
import ecommerce.dto.MemberStatsResponse
import ecommerce.dto.ProductStatResponse
import ecommerce.entity.Cart
import ecommerce.entity.Member
import ecommerce.repository.CartJpaRepository
import ecommerce.repository.CartStaticsRepository
import ecommerce.repository.OptionJpaRepository
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
    private val optionRepository: OptionJpaRepository,
    private val cartStaticsRepository: CartStaticsRepository,
) {
    fun addToCart(
        member: Member,
        request: CartRequest,
    ) {
        val product =
            productRepository.findByIdOrNull(request.productId)
                ?: throw NoSuchElementException("Product not found: ${request.productId}")

        val option =
            optionRepository.findByIdOrNull(request.optionId)
                ?: throw NoSuchElementException("Option not found: ${request.optionId}")

        require(option.product?.id == product.id) {
            "Option ${request.optionId} does not belong to product ${request.productId}"
        }

        val qty = request.quantity.coerceAtLeast(1)

        val existing = cartRepository.findByMemberIdAndOptionId(member.id, option.id)
        if (existing != null) {
            existing.quantity += qty
        } else {
            cartRepository.save(
                Cart(
                    member = member,
                    product = product,
                    option = option,
                    quantity = qty,
                ),
            )
        }
    }

    fun getCartItems(memberId: Long): List<Cart> = cartRepository.findByMemberId(memberId)

    fun getCartItemsPaginated(
        memberId: Long,
        pageable: Pageable,
    ): Page<Cart> = cartRepository.findByMemberId(memberId, pageable)

    fun removeFromCart(
        memberId: Long,
        optionId: Long,
    ) {
        cartRepository.deleteByMemberIdAndOptionId(memberId, optionId)
    }

    fun getTop5MostAddedProducts(): List<ProductStatResponse> = cartStaticsRepository.getTop5MostAddedProducts()

    fun getRecentlyActiveMembers(): List<MemberStatsResponse> = cartStaticsRepository.getRecentlyActiveMembers()
}
