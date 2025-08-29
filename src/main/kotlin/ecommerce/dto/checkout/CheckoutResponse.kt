package ecommerce.dto.checkout

import ecommerce.dto.order.OrderItemResponse

data class CheckoutResponse(
    var id: String,
    var client_secret: String?,
    var amount: Int,
    var currency: String,
    var status: String,
    var payment_method: String?,
    var orderId: Long,
    var orderStatus: String,
    var items: List<OrderItemResponse>,
)
