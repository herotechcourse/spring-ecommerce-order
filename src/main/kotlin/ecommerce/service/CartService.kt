package ecommerce.service

import ecommerce.dto.CartItem
import ecommerce.dto.MemberResponse
import ecommerce.dto.TopProductStatResponse
import ecommerce.entity.CartItemEntity
import ecommerce.repository.CartItemRepositoryJpa
import ecommerce.repository.CartRepository
import ecommerce.repository.ProductRepositoryJpa
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val productRepositoryJpa: ProductRepositoryJpa,
    private val cartItemRepositoryJpa: CartItemRepositoryJpa,
) {
    fun addToCart(
        memberId: Long,
        productId: Long,
    ) {
        productRepositoryJpa.findById(productId)
            ?: throw NoSuchElementException("Product not found")
        cartRepository.add(memberId, productId)
    }

    fun removeFromCart(
        memberId: Long,
        productId: Long,
    ) {
        return cartRepository.remove(memberId, productId)
    }

    fun getCartItems(memberId: Long): List<CartItem> {
        return cartRepository.getCartItems(memberId)
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
