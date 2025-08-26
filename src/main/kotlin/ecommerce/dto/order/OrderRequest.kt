package ecommerce.dto.order

import ecommerce.model.Currency

data class OrderRequest(
    var items: List<OrderItemRequest>,
    val paymentMethod: String,
    val currency: Currency = Currency.USD,
)

data class OrderItemRequest(
    val productId: Long,
    val optionId: Long,
    val quantity: Int,
)
