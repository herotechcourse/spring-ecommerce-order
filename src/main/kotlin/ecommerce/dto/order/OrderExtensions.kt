package ecommerce.dto.order

import ecommerce.model.Order
import ecommerce.model.OrderItem

fun Order.toResponse(): OrderResponse {
    return OrderResponse(
        id = this.id ?: 0L,
        orderDate = this.createdAt ?: throw IllegalStateException("Order creation date is null"),
        orderStatus = this.orderStatus,
        paymentStatus = this.paymentStatus,
        currency = this.currency,
        totalAmount = this.totalAmount,
        stripeCheckoutSessionId = this.stripeCheckoutSessionId,
        stripePaymentIntentId = this.stripePaymentIntentId,
        orderItems = this.orderItems.map { it.toResponse() },
    )
}

fun OrderItem.toResponse(): OrderItemResponse {
    return OrderItemResponse(
        id = this.id ?: 0L,
        productName = this.productName,
        optionName = this.optionName,
        quantity = this.quantity,
        unitPrice = this.unitPrice,
        totalPrice = this.totalPrice,
    )
}
