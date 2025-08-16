package ecommerce.model

import ecommerce.dto.OrderResponse
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class Order(
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member,
    @OneToMany(cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    var orderItems: List<OrderItem> = emptyList(),
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.PRE_PAYMENT,
    @Column(nullable = false)
    var paymentAmount: Double = 0.0,
    @Column(nullable = false)
    var currency: String = "USD",
    @Column
    var paymentMethod: String? = null,
    @Column
    var checkoutSessionId: String? = null,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    init {
        this.paymentAmount += totalAmount()
    }

    fun addItem(item: OrderItem) {
        this.orderItems = orderItems.plus(item)
    }

    fun totalAmount(): Double {
        return orderItems.sumOf { it.product.price * it.quantity }
    }

    companion object {
        fun to(order: Order): OrderResponse {
            return OrderResponse(
                orderDate = order.createdAt,
                orderStatus = order.status,
                orderItems = order.orderItems.map { OrderItem.to(it) },
                checkoutSessionId = order.checkoutSessionId,
                paymentAmount = order.paymentAmount,
            )
        }
    }
}
