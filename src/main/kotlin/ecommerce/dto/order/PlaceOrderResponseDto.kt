package ecommerce.dto.order

data class PlaceOrderResponseDto(
    val orderId: Long,
    val checkoutSession: String,
)
