package ecommerce.dto

class OrderItemDTO(
    val optionId: Long,
    val quantity: Int,
    val unitPrice: Double,
    var id: Long? = null,
)
