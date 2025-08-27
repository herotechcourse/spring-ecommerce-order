package ecommerce.service

import ecommerce.dto.CartItemRequest
import ecommerce.dto.CartItemResponse
import ecommerce.exception.ElementNotFoundException
import ecommerce.mapper.toDto
import ecommerce.model.CartHistory
import ecommerce.model.CartItem
import ecommerce.repository.CartHistoryJpaRepository
import ecommerce.repository.CartJpaRepository
import ecommerce.repository.OptionJpaRepository
import ecommerce.repository.ProductJpaRepository
import ecommerce.repository.getByIdOrThrow
import ecommerce.repository.getByMemberId
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class CartService(
    private val cartJpaRepository: CartJpaRepository,
    private val productJpaRepository: ProductJpaRepository,
    private val optionJpaRepository: OptionJpaRepository,
    private val cartHistoryJpaRepository: CartHistoryJpaRepository,
) {
    fun addOrUpdateCartItem(
        memberId: Long,
        request: CartItemRequest,
    ): CartItemResponse {
        val product = productJpaRepository.getByIdOrThrow(request.productId)
        val cart = cartJpaRepository.getByMemberId(memberId)
        val option = optionJpaRepository.getByIdOrThrow(request.optionId)
        option.reduceOptionQuantity(request.quantity)
        optionJpaRepository.save(option)

        val cartItem = CartItem(cart, product, option, request.quantity)
        cart.addOrUpdateCartItem(cartItem)
        val savedCart = cartJpaRepository.save(cart)

        val savedCartItem = savedCart.cartProducts.last { it.product.id == request.productId && it.option.id == request.optionId }

        val history = CartHistory(cartProductId = savedCartItem.id, status = "ADDED")
        cartHistoryJpaRepository.save(history)

        return cartItem.toDto()
    }

    fun getCartItems(
        memberId: Long,
        page: Int,
        size: Int,
        sortBy: String,
        ascending: Boolean,
    ): Page<CartItemResponse> {
        val sort = if (ascending) Sort.by(sortBy).ascending() else Sort.by(sortBy).descending()
        val pageable = PageRequest.of(page, size, sort)
        val cart = cartJpaRepository.getByMemberId(memberId)
        val products = cart.cartProducts
        val start = pageable.offset.toInt()
        val end = (start + pageable.pageSize).coerceAtMost(products.size)
        val productsInPage = products.subList(start, end).toList()
        return PageImpl(productsInPage.map { it.toDto() }, pageable, products.size.toLong())
    }

    fun deleteProductFromCart(
        memberId: Long,
        productId: Long,
    ) {
        val cart = cartJpaRepository.getByMemberId(memberId)
        val deleted = cart.deleteCartProduct(productId)
        if (!deleted) throw ElementNotFoundException("Element not in the cart")
        cartJpaRepository.save(cart)
    }

    fun deleteCartProducts(memberId: Long) {
        val cart = cartJpaRepository.getByMemberId(memberId)
        val deleted = cart.clearCartProducts()
        if (!deleted) throw ElementNotFoundException("Cart is empty")
        cartJpaRepository.save(cart)
    }
}
