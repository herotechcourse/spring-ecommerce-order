package ecommerce.dto

data class OrderItemResponse(
    val productId: Long,
    val productName: String,
    val optionId: Long,
    val optionName: String,
    val quantity: Int,
    val price: Double,
)
