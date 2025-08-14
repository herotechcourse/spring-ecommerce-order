package ecommerce.dto

data class CartItemResponse(
    val id: Long,
    val productName: String,
    val optionName: String,
    val quantity: Int,
    val productPrice: Double,
    val productImageUrl: String,
)
