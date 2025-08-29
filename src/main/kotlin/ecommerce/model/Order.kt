package ecommerce.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(
    name = "orders",
    indexes = [
        Index(name = "idx_order_member_id", columnList = "member_id"),
    ],
)
class Order(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,
    @Column(name = "stripe_checkout_session_id", nullable = true)
    var stripeCheckoutSessionId: String? = null,
    @Column(name = "stripe_payment_intent_id", nullable = true)
    var stripePaymentIntentId: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    var orderStatus: OrderStatus = OrderStatus.PENDING,
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    var paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    val currency: Currency = Currency.EUR,
    @Column(name = "total_amount", nullable = false)
    var totalAmount: Double = 0.0,
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    val orderItems: MutableList<OrderItem> = mutableListOf(),
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    fun calculateTotalAmount(): Double {
        totalAmount = orderItems.sumOf { it.getTotalAmount() }
        return totalAmount
    }

    fun addOrderItem(orderItem: OrderItem) {
        orderItems.add(orderItem)
        calculateTotalAmount()
    }

    fun isPaymentCompleted(): Boolean = paymentStatus == PaymentStatus.COMPLETED

    fun isPending(): Boolean = orderStatus == OrderStatus.PENDING

    fun isConfirmed(): Boolean = orderStatus == OrderStatus.CONFIRMED

    fun canBeCancelled(): Boolean = orderStatus in listOf(OrderStatus.PENDING, OrderStatus.CONFIRMED)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Order) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Order(id=$id, stripeCheckoutSessionId='$stripeCheckoutSessionId', orderStatus=$orderStatus, paymentStatus=$paymentStatus)"
    }
}
