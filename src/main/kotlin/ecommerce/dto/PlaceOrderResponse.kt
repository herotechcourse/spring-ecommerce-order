package ecommerce.dto

class PlaceOrderResponse(
    val orderId: Long,
    val status: String,
    val totalAmount: Double,
    val failureCode: String? = null,
    val failureMessage: String? = null,
)
