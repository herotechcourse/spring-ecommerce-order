package ecommerce.dto

data class OrderResponse(
    val orderId: Long,
    val status: String,
    val totalAmount: Double,
    val failureCode: String? = null,
    val failureMessage: String? = null,
)
