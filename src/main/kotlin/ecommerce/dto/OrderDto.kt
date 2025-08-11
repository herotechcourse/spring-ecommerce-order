package ecommerce.dto
import java.time.LocalDateTime

class OrderDto(
    val createdAt: LocalDateTime,
    var status: String,
    val items: List<OrderItemDto>,
    val paymentAmount: Int,
    val currency: String,
    val paymentMethod: String,
    var checkoutSessionId: String? = null,
)
