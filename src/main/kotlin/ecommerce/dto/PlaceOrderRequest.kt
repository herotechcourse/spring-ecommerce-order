package ecommerce.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class PlaceOrderRequest(
    @field:NotEmpty
    val items: List<OrderItemRequest>,

    @field:NotNull
    val paymentData: PaymentRequest,
)
