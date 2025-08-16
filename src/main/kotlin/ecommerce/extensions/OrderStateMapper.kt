package ecommerce.extensions

import ecommerce.dto.options.OptionQuantityDto
import ecommerce.dto.order.PlaceOrderRequestDto
import ecommerce.enums.OrderStatus
import ecommerce.model.Order
import ecommerce.model.OrderItem
import ecommerce.model.User
import java.time.LocalDateTime

object OrderStateMapper {
    fun newPending(
        user: User,
        amount: Double,
        req: PlaceOrderRequestDto,
        now: LocalDateTime = LocalDateTime.now(),
    ): Order =
        Order(
            user,
            "",
            amount,
            req.currency,
            req.paymentMethodId,
            OrderStatus.PENDING,
            null,
            now,
        )

    fun applyFailed(
        order: Order,
        reason: String,
        stripeSessionId: String? = null,
    ): Order {
        order.status = OrderStatus.FAILED
        order.failureReason = reason
        if (stripeSessionId != null) order.stripeSessionId = stripeSessionId
        return order
    }

    fun applyPaid(
        order: Order,
        sessionId: String,
        items: List<OptionQuantityDto>,
    ): Order {
        order.status = OrderStatus.PAID
        order.failureReason = null
        order.stripeSessionId = sessionId
        items.forEach { (opt, qty) -> order.items.add(OrderItem(order, opt, qty)) }
        return order
    }
}
