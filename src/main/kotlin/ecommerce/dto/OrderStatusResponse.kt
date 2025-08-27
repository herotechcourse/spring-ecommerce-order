package ecommerce.dto

import ecommerce.model.OrderStatus

data class OrderStatusResponse(
    val orderId: Long,
    val status: OrderStatus,
)
