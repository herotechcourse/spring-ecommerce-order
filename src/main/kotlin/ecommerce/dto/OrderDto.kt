package ecommerce.dto
import java.time.LocalDateTime

class OrderDto(
    val createdAt: LocalDateTime,
    var status: String,
    val items: MutableList<OrderItemDto>,
    val paymentAmount: Double,
    val currency: String,
    val paymentMethod: String,
    var checkoutSessionId: String? = null,
    val id: Long,
)
