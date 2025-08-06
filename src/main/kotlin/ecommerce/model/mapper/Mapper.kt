package ecommerce.model.mapper

import ecommerce.dto.CartItemResponse
import ecommerce.dto.OptionResponse
import ecommerce.dto.ProductResponse
import ecommerce.model.CartItem
import ecommerce.model.Option
import ecommerce.model.Product

object CartItemMapper {
    fun toResponse(cartItem: CartItem): CartItemResponse {
        return CartItemResponse(
            quantity = cartItem.quantity,
            productId = cartItem.product.id ?: 0,
            productName = cartItem.product.name,
            productPrice = cartItem.product.price,
            productImageUrl = cartItem.product.imageUrl,
        )
    }
}

object OptionMapper {
    fun toOptionResponse(option: Option) =
        OptionResponse(
            id = option.id,
            name = option.name,
            quantity = option.quantity,
        )
}

object ProductMapper {
    fun toProductDto(entity: Product) =
        ProductResponse(
            id = entity.id,
            name = entity.name,
            price = entity.price,
            imageUrl = entity.imageUrl,
        )
}
