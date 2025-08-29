package ecommerce.dto.order

import ecommerce.model.Currency
import ecommerce.model.OrderStatus
import ecommerce.model.PaymentStatus
import java.time.LocalDateTime

data class OrderResponse(
    val id: Long,
    val orderDate: LocalDateTime,
    val orderStatus: OrderStatus,
    val paymentStatus: PaymentStatus,
    val currency: Currency,
    val totalAmount: Double,
    val stripeCheckoutSessionId: String?,
    val stripePaymentIntentId: String?,
    val orderItems: List<OrderItemResponse>,
)

data class OrderItemResponse(
    val id: Long,
    val productName: String,
    val optionName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
)
