package ecommerce.dto

data class CreateOrderRequest(
    val productOptionId: Long,
    val quantity: Int,
    val paymentMethod: String,
    val currency: String,
)
