package ecommerce.service

import ecommerce.dto.OrderResponse
import ecommerce.entity.Member
import ecommerce.entity.Option
import ecommerce.entity.Order
import ecommerce.entity.OrderItem
import ecommerce.entity.PaymentAttempt
import ecommerce.entity.enumerated.OrderStatus
import ecommerce.entity.enumerated.PaymentAttemptStatus
import ecommerce.exception.InsufficientStockException
import ecommerce.exception.OrderAlreadyFinalizedException
import ecommerce.repository.CartItemJpaRepository
import ecommerce.repository.OptionJpaRepository
import ecommerce.repository.OrderJpaRepository
import ecommerce.repository.PaymentAttemptJpaRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val orderRepo: OrderJpaRepository,
    private val optionRepo: OptionJpaRepository,
    private val cartItemRepo: CartItemJpaRepository,
    private val paymentAttemptRepo: PaymentAttemptJpaRepository,
) {
    @Transactional
    fun createPending(
        member: Member,
        items: List<Pair<Option, Int>>,
    ): Order {
        require(items.isNotEmpty()) { "Order must contain at least one item" }
        items.forEach { (_, qty) -> require(qty > 0) { "Quantity must be > 0" } }

        val aggregated: List<Pair<Option, Int>> =
            items
                .groupBy { it.first.id }
                .map { (_, list) ->
                    val option = list.first().first
                    val qtySum = list.sumOf { it.second }
                    option to qtySum
                }

        val order = Order(member)

        var total = 0.0
        for ((option, qty) in aggregated) {
            val price = option.product.price
            val item =
                OrderItem(
                    order = order,
                    productId = option.product.id,
                    optionId = option.id,
                    productNameSnapshot = option.product.name,
                    priceSnapshot = option.product.price,
                    quantity = qty,
                )
            order.items += item
            total += price * qty
        }

        order.totalAmount = total
        return orderRepo.save(order)
    }

    @Transactional
    fun startPaymentAttempt(
        orderId: Long,
        provider: String = "stripe",
    ): PaymentAttempt {
        val order = orderRepo.findById(orderId).orElseThrow()
        if (order.status != OrderStatus.PENDING) throw OrderAlreadyFinalizedException()
        val attempt = PaymentAttempt(order = order, provider = provider)
        return paymentAttemptRepo.save(attempt)
    }

    @Transactional
    fun finalizePaid(
        orderId: Long,
        externalId: String?,
    ): Order {
        val order = orderRepo.findById(orderId).orElseThrow()
        if (order.status != OrderStatus.PENDING) throw OrderAlreadyFinalizedException()

        for (item in order.items) {
            val affected = optionRepo.decrementStockIfEnough(item.optionId, item.quantity)
            if (affected != 1) throw InsufficientStockException()
        }

        for (item in order.items) {
            cartItemRepo.deleteByCartMemberIdAndOptionId(order.member.id, item.optionId)
        }

        order.status = OrderStatus.PAID
        order.touch()

        val attempts =
            paymentAttemptRepo.findAll()
                .filter { it.order.id == orderId }
                .sortedBy { it.id }
        attempts.lastOrNull()?.apply {
            status = PaymentAttemptStatus.APPROVED
            externalId?.let { this.externalId = it }
        }

        return order
    }

    @Transactional
    fun markFailed(
        orderId: Long,
        failureCode: String?,
        failureMessage: String?,
    ) {
        val order = orderRepo.findById(orderId).orElseThrow()
        if (order.status != OrderStatus.PENDING) return
        order.status = OrderStatus.FAILED
        order.touch()

        val attempts =
            paymentAttemptRepo.findAll()
                .filter { it.order.id == orderId }
                .sortedBy { it.id }
        attempts.lastOrNull()?.apply {
            status = PaymentAttemptStatus.REJECTED
            this.failureCode = failureCode
            this.failureMessage = failureMessage
        }
    }

    fun toResponse(
        order: Order,
        failureCode: String? = null,
        failureMessage: String? = null,
    ) = OrderResponse(
        order.id,
        order.status.name,
        order.totalAmount,
        failureCode,
        failureMessage,
    )
}
