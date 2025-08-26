package ecommerce.dto

class OrderPlacementResponse(
    val status: String = OrderResponseStatus.SUCCESS.name,
    val orderId: Long? = null,
    val message: String,
)
