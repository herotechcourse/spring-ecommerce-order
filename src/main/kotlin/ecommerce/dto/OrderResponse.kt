package ecommerce.dto

data class OrderResponse(
    val order: OrderDto,
    val payment: PaymentResponse,
)
