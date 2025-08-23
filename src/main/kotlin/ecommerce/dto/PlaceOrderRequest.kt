package ecommerce.dto

import jakarta.validation.constraints.NotNull

data class PlaceOrderRequest(
    @field:NotNull
    val orderItems: List<OrderItemRequest>,
    @field:NotNull
    val paymentRequest: PaymentRequest,
)
