package ecommerce.dto.order

data class OrderResponse(
    val message: String,
    val paymentIntentId: String? = null,
)
