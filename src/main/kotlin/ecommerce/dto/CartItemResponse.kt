package ecommerce.dto

class CartItemResponse(
    val productId: Long,
    val quantity: Int,
    val productName: String,
    val productPrice: Double,
    val productImageUrl: String,
    val optionId: Long,
    val optionName: String,
) : PageItem
