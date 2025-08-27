package ecommerce.mapper

import ecommerce.dto.OrderItemResponse
import ecommerce.dto.OrderResponse
import ecommerce.dto.stripe.PaymentDto
import ecommerce.model.Order
import ecommerce.model.OrderItem
import ecommerce.model.Payment

fun Order.toResponse(): OrderResponse {
    return OrderResponse(
        orderId = this.id,
        memberId = this.member.id,
        orderDate = this.orderDate,
        status = this.status,
        items = this.items.map { it.toResponse() },
        payment = this.payment?.toDto(),
    )
}

fun OrderItem.toResponse(): OrderItemResponse {
    return OrderItemResponse(
        productId = this.product.id,
        productName = this.product.name,
        optionId = this.option.id,
        optionName = this.option.name,
        quantity = this.quantity,
        price = this.price,
    )
}

fun Payment.toDto(): PaymentDto {
    return PaymentDto(
        amount = this.amount,
        currency = this.currency,
        status = this.status,
        createdAt = this.createdAt,
    )
}
