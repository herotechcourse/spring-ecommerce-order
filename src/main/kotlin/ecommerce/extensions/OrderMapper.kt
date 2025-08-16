package ecommerce.extensions

import ecommerce.dto.order.OrderItemResponseDto
import ecommerce.dto.order.OrderResponseDto
import ecommerce.model.Order
import ecommerce.model.OrderItem

fun Order.toDto(): OrderResponseDto =
    OrderResponseDto(
        id,
        createdAt,
        status.name,
        stripeSessionId,
        amount,
        currency,
        paymentMethod,
        items.map { it.toDto() },
    )

fun OrderItem.toDto(): OrderItemResponseDto =
    OrderItemResponseDto(
        productOption.id,
        productOption.name,
        quantity,
        productOption.price,
        productOption.price * quantity,
    )
