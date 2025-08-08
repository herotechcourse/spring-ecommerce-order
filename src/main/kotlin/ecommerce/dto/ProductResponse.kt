package ecommerce.dto

data class ProductResponse(
    val id: Long,
    val name: String,
    val price: Double,
    val imageUrl: String,
)
