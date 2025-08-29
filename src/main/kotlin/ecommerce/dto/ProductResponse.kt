package ecommerce.dto

data class ProductResponse(
    val id: Long,
    val name: String,
    val price: Int,
    val imageUrl: String,
    val options: List<OptionResponse>,
)
