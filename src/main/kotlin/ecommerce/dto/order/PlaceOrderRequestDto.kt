package ecommerce.dto.order

data class PlaceOrderRequestDto(
    val currency: String,
    val paymentMethodId: String,
)
