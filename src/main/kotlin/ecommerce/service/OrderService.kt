package ecommerce.service

import ecommerce.dto.OrderItemRequest
import ecommerce.entity.Member
import ecommerce.entity.Order
import ecommerce.entity.OrderItem
import ecommerce.entity.enumerated.OrderStatus
import ecommerce.repository.OptionJpaRepository
import ecommerce.repository.OrderJpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepo: OrderJpaRepository,
    private val optionRepo: OptionJpaRepository,
) {
    @Transactional(readOnly = true)
    fun findById(orderId: Long): Order {
        return orderRepo.findById(orderId).orElseThrow()
    }

    @Transactional
    fun create(
        member: Member,
        orderItems: List<OrderItemRequest>,
    ): Order {
        val order = Order(member)

        val newItems =
            orderItems.map { orderItemRequest ->
                val option = optionRepo.findById(orderItemRequest.optionId).orElseThrow()
                OrderItem(
                    order = order,
                    productId = option.product.id,
                    optionId = option.id,
                    productNameSnapshot = option.product.name,
                    priceSnapshot = option.product.price,
                    quantity = orderItemRequest.quantity,
                )
            }

        order.items.addAll(newItems)
        if (order.items.isEmpty()) {
            throw IllegalArgumentException("Cannot create an order with no items.")
        }
        order.totalAmount = newItems.sumOf { it.subtotal }
        return orderRepo.save(order)
    }

    @Transactional
    fun updateOrderStatus(
        orderId: Long,
        status: OrderStatus,
    ) {
        val order = orderRepo.findById(orderId).orElseThrow()
        order.status = status
        order.touch()
    }
}
