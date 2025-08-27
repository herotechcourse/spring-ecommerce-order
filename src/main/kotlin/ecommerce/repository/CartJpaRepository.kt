package ecommerce.repository

import ecommerce.exception.ElementNotFoundException
import ecommerce.model.Cart
import org.springframework.data.jpa.repository.JpaRepository

fun CartJpaRepository.getByMemberId(memberId: Long): Cart =
    findByMemberId(memberId)
        ?: throw ElementNotFoundException("No cart found")

fun CartJpaRepository.getByMemberIdAndValidateNotEmpty(memberId: Long): Cart {
    val cart = getByMemberId(memberId)
    if (cart.cartProducts.isEmpty()) {
        throw IllegalStateException("Cannot place an order with an empty cart.")
    }
    return cart
}

interface CartJpaRepository : JpaRepository<Cart, Long> {
    fun findByMemberId(memberId: Long): Cart?
}
