package ecommerce.mapper

import ecommerce.dto.OrderDto
import ecommerce.dto.OrderItemResponse
import ecommerce.model.Order
import ecommerce.model.OrderItem

class OrderMapper

fun Order.toOrderDto() =
    OrderDto(
        paymentIntentId = paymentIntentId ?: "Not Available",
        orderId = id,
        orderItems = orderItems.map { it.toOrderItemResponse() },
        createdAt = createdAt!!,
        totalAmount = totalAmount.toDouble(),
    )

fun OrderItem.toOrderItemResponse() =
    OrderItemResponse(
        productName = productName,
        optionName = optionName,
        quantity = quantity,
        price = price,
    )
