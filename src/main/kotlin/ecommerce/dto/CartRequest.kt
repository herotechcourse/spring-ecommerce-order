package ecommerce.dto

data class CartRequest(
    val productId: Long,
    val optionId: Long,
    val quantity: Int = 1,
)
