package ecommerce.service

import ecommerce.dto.cart.AddToCartRequest
import ecommerce.exception.NotFoundException
import ecommerce.model.CartItem
import ecommerce.repository.CartItemRepository
import ecommerce.repository.CartRepository
import ecommerce.repository.CartStatisticsRepository
import ecommerce.repository.ProductOptionRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class CartItemService(
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val productOptionRepository: ProductOptionRepository,
    private val cartStatisticsRepository: CartStatisticsRepository,
) {
    @Transactional
    fun addCartItem(
        request: AddToCartRequest,
        cartId: Long,
    ): CartItem {
        val cart =
            cartRepository.findById(cartId).getOrNull()
                ?: throw NotFoundException("Cart not found")
        val productOption =
            productOptionRepository.findById(request.productOptionId).getOrNull()
                ?: throw NotFoundException("Product Option not found")
        val cartItem =
            CartItem(
                cart = cart,
                productOption = productOption,
                quantity = request.newProductOptionQuantity,
            )
        return cartItemRepository.save(cartItem)
    }

    @Transactional
    fun updateCartItem(
        request: AddToCartRequest,
        cartId: Long,
        cartItemId: Long,
    ): CartItem {
        val cart =
            cartRepository.findById(cartId).getOrNull()
                ?: throw NotFoundException("Cart not found")
        val cartItem =
            cartItemRepository.findById(cartItemId).getOrNull()
                ?: throw NotFoundException("cart Item not found")
        if (cartItem.cart.id != cart.id) {
            throw IllegalArgumentException("Cart item does not belong to cartId=$cartId")
        }
        cartItem.quantity = request.newProductOptionQuantity
        return cartItemRepository.save(cartItem)
    }

    @Transactional
    fun deleteCartItemById(
        cartItemId: Long,
        cartId: Long,
    ) {
        cartRepository.findById(cartId).getOrNull()
            ?: throw NotFoundException("Cart not found")
        cartItemRepository.findById(cartItemId).getOrNull()
            ?: throw NotFoundException("Cart Item not found")
        cartItemRepository.deleteById(cartItemId)
    }

    @Transactional
    fun deleteAllCartItemsByCartId(cartId: Long) {
        cartRepository.findById(cartId).getOrNull()
            ?: throw NotFoundException("Cart not found")
        cartStatisticsRepository.deleteByCartId(cartId)
        cartItemRepository.deleteByCartId(cartId)
    }
}
