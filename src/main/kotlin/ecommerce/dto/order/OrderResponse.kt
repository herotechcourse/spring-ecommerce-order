package ecommerce.dto.order

import ecommerce.enums.OrderStatus
import ecommerce.enums.PaymentOption
import java.time.LocalDateTime

class OrderResponse(
    val orderId: Long,
    val createdAt: LocalDateTime,
    val status: OrderStatus,
    val totalAmount: Double,
    val paymentId: String,
    val paymentOption: PaymentOption,
    val products: List<OrderProductResponse>,
)
