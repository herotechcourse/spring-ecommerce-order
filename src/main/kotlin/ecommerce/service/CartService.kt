package ecommerce.service

import ecommerce.dto.CartItemRequest
import ecommerce.dto.CartItemResponse
import ecommerce.mapper.toDto
import ecommerce.model.Cart
import ecommerce.model.CartHistory
import ecommerce.model.CartItem
import ecommerce.repository.CartHistoryJpaRepository
import ecommerce.repository.CartJpaRepository
import ecommerce.repository.OptionJpaRepository
import ecommerce.repository.getByIdOrThrow
import ecommerce.repository.getByMemberId
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class CartService(
    private val cartJpaRepository: CartJpaRepository,
    private val cartHistoryJpaRepository: CartHistoryJpaRepository,
    private val optionJpaRepository: OptionJpaRepository,
) {
    fun addOrUpdateCartItem(
        memberId: Long,
        request: CartItemRequest,
    ): CartItemResponse {
        val option = optionJpaRepository.getByIdOrThrow(request.optionId)
        val cart = cartJpaRepository.getByMemberId(memberId)
        val cartItem = CartItem(cart, option, request.quantity)
        cart.addOrUpdateCartItem(cartItem)

        cartHistoryJpaRepository.save(CartHistory(cart.member, option, request.quantity))

        cartJpaRepository.save(cart)
        return cart.cartProducts
            .first { it.option.id == option.id }
            .toDto()
    }

    fun getCartItems(
        memberId: Long,
        pageable: Pageable,
    ): Page<CartItemResponse> {
        val cart = cartJpaRepository.getByMemberId(memberId)
        val products = cart.cartProducts
        val start = pageable.offset.toInt()
        val end = (start + pageable.pageSize).coerceAtMost(products.size)
        val productsInPage = products.subList(start, end).toList()
        return PageImpl(productsInPage.map { it.toDto() }, pageable, products.size.toLong())
    }

    fun deleteProductFromCart(
        memberId: Long,
        optionId: Long,
    ) {
        val cart = cartJpaRepository.getByMemberId(memberId)
        cart.deleteCartProduct(optionId)
        cartJpaRepository.save(cart)
    }

    fun cartCheckOut(cart: Cart) {
        cart.cartProducts.forEach { it.option.reduceOptionQuantity(it.quantity) }
        cart.cleanCart()
    }
}
