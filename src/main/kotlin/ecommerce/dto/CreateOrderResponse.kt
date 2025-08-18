package ecommerce.dto

data class CreateOrderResponse(
    val orderId: Long,
    val paymentIntentId: String,
)
