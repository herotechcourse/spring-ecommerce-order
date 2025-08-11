package ecommerce.service

import ecommerce.dto.order.OrderItemResponse
import ecommerce.dto.order.OrderResponse
import ecommerce.repository.OrderItemJpaRepository
import ecommerce.repository.OrderJpaRepository
import ecommerce.repository.PaymentJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class OrderQueryService(
    private val orderRepository: OrderJpaRepository,
    private val orderItemRepository: OrderItemJpaRepository,
    private val paymentRepository: PaymentJpaRepository,
) {
    fun getOrdersForMember(
        memberId: Long,
        pageable: Pageable,
    ): Page<OrderResponse> {
        return orderRepository.findByMemberId(memberId, pageable)
            .map { order ->
                val items =
                    orderItemRepository.findByOrderId(order.id)
                        .map { item ->
                            OrderItemResponse(
                                productId = item.productId,
                                productName = item.productName,
                                optionId = item.optionId,
                                optionName = item.optionName,
                                unitPrice = item.unitPrice,
                                quantity = item.quantity,
                                lineTotal = item.lineTotal,
                            )
                        }

                val payment = paymentRepository.findByOrderId(order.id)

                OrderResponse(
                    id = order.id,
                    orderedAt = order.orderedAt,
                    status = order.status,
                    totalAmount = order.totalAmount,
                    currency = order.currency,
                    paymentIntentId = payment?.intentId,
                    paymentStatus = payment?.status,
                    items = items,
                )
            }
    }

    fun getOrderDetail(
        memberId: Long,
        orderId: Long,
    ): OrderResponse {
        val order =
            orderRepository.findById(orderId)
                .orElseThrow { NoSuchElementException("Order not found: $orderId") }

        if (order.member.id != memberId) {
            throw IllegalAccessException("You are not allowed to view this order")
        }

        val items =
            orderItemRepository.findByOrderId(order.id)
                .map { item ->
                    OrderItemResponse(
                        productId = item.productId,
                        productName = item.productName,
                        optionId = item.optionId,
                        optionName = item.optionName,
                        unitPrice = item.unitPrice,
                        quantity = item.quantity,
                        lineTotal = item.lineTotal,
                    )
                }

        val payment = paymentRepository.findByOrderId(order.id)

        return OrderResponse(
            id = order.id,
            orderedAt = order.orderedAt,
            status = order.status,
            totalAmount = order.totalAmount,
            currency = order.currency,
            paymentIntentId = payment?.intentId,
            paymentStatus = payment?.status,
            items = items,
        )
    }
}
