package ecommerce.dto.order

data class PlaceOrderRequest(
    val productId: Long,
    val optionId: Long,
    val quantity: Int,
)
