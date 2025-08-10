package ecommerce.dto.order

data class PlaceOrderResponse(
    val orderStatus: String,
    val paymentIntentId: String?,
    val amount: Long?,
    val currency: String?,
    val items: List<PurchasedItem>
)

data class PurchasedItem(
    val productId: Long,
    val optionId: Long,
    val quantity: Int
)