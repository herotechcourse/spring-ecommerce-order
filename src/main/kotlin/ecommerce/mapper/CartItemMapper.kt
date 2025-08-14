package ecommerce.mapper

import ecommerce.dto.CartItemResponse
import ecommerce.model.CartItem
import ecommerce.model.OrderItem

class CartItemMapper

fun CartItem.toDto(): CartItemResponse =
    CartItemResponse(
        id = id,
        productName = option.product!!.name,
        optionName = option.name,
        quantity = quantity,
        productPrice = option.product!!.price.toDouble(),
        productImageUrl = option.product!!.imageUrl,
    )

fun CartItem.toOrderItem(): OrderItem =
    OrderItem(
        optionId = option.id,
        productName = option.product!!.name,
        optionName = option.name,
        createdAt = createdAt,
        lastUpdatedAt = lastUpdatedAt,
        quantity = quantity,
        price = option.product!!.price,
    )
