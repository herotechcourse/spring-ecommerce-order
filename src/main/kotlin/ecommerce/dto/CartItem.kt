package ecommerce.dto

data class CartItem(
    val productId: Long,
    val name: String,
    val price: Double,
    val quantity: Int,
)
