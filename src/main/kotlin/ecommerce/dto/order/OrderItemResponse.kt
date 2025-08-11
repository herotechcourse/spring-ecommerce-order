package ecommerce.dto.order

data class OrderItemResponse(
    val productId: Long,
    val productName: String,
    val optionId: Long,
    val optionName: String,
    val unitPrice: Long,
    val quantity: Int,
    val lineTotal: Long
)