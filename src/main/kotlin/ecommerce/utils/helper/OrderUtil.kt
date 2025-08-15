package ecommerce.utils.helper

import ecommerce.dto.cartProduct.CartProductDto

object OrderUtil {
    fun calculateTotal(cartProductDto: List<CartProductDto>): Double {
        return cartProductDto.sumOf { it.price * it.quantity }
    }
}
