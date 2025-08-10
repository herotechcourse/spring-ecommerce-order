package ecommerce.service

import ecommerce.dto.MemberResponse
import ecommerce.dto.TopProductStatResponse
import ecommerce.entity.CartEntity
import ecommerce.entity.CartItemEntity
import ecommerce.repository.CartItemRepositoryJpa
import ecommerce.repository.CartRepository
import ecommerce.repository.CartRepositoryJpa
import ecommerce.repository.OptionRepositoryJpa
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val cartRepositoryJpa: CartRepositoryJpa,
    private val optionRepositoryJpa: OptionRepositoryJpa,
    private val cartItemRepositoryJpa: CartItemRepositoryJpa,
) {
    @Transactional
    fun addToCart(
        memberId: Long,
        productOptionId: Long,
    ) {
        val productOption =
            optionRepositoryJpa.findById(productOptionId)
                .orElseThrow { NoSuchElementException("Product Option not found") }

        val product =
            productOption.product
                ?: throw IllegalStateException("Product associated with the option is null")

        // Find or create cart
        var cart = cartRepositoryJpa.findByMemberId(memberId)

        if (cart == null) {
            cart =
                cartRepositoryJpa.save(
                    CartEntity(
                        id = null,
                        memberId = memberId,
                    ),
                )
        }

        // Find existing cart item for same product option
        val existingItem = cartItemRepositoryJpa.findByCartIdAndProductOptionId(cart.id!!, productOptionId)

        if (existingItem != null) {
            existingItem.quantity += 1
            cartItemRepositoryJpa.save(existingItem)
        } else {
            val newItem =
                CartItemEntity(
                    cart = cart,
                    product = product,
                    productOption = productOption,
                    quantity = 1,
                    createdAt = LocalDateTime.now(),
                )
            cartItemRepositoryJpa.save(newItem)
        }
    }

    fun removeFromCart(
        memberId: Long,
        productOptionId: Long,
    ) {
        return cartRepository.removeByOptionId(memberId, productOptionId)
    }

    @Transactional
    fun getCartItems(memberId: Long): List<CartItemEntity> {
        val cart = cartRepositoryJpa.findByMemberId(memberId)
        return cart?.cartItems ?: listOf()
    }

    fun findTop5ProductsInLast30Days(): List<TopProductStatResponse> {
        return cartRepository.findTop5ProductsInLast30Days()
    }

    fun findMembersWithCartActivityInLast7Days(): List<MemberResponse> {
        return cartRepository.findMembersWithCartActivityInLast7Days()
    }

    fun getAllCartItems(
        page: Int,
        size: Int,
        sortBy: String = "created_at",
        direction: Sort.Direction = Sort.Direction.ASC,
    ): Page<CartItemEntity> {
        val pageable = PageRequest.of(page, size, Sort.by(direction, sortBy))
        return cartItemRepositoryJpa.findAll(pageable)
    }

    fun getItemsByQuantity(
        quantity: Int,
        page: Int,
        size: Int,
    ): Page<CartItemEntity> {
        val pageable = PageRequest.of(page, size)

        return cartItemRepositoryJpa.findAllByQuantity(quantity, pageable)
    }
}
