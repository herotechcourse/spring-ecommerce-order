package ecommerce.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class PlaceOrderRequest(
    @field:NotNull
    @field:Positive
    val cartId: Long,

    @field:NotNull
    val paymentData: PaymentRequest,
)
