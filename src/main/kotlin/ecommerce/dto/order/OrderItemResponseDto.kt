package ecommerce.dto.order

data class OrderItemResponseDto(
    val optionId: Long,
    val optionName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
)
