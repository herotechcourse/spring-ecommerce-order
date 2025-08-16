package ecommerce.model

import ecommerce.enums.OrderStatus
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
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
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    val user: User,
    @Column(name = "stripe_session_id", nullable = false)
    var stripeSessionId: String,
    @Column(nullable = false)
    var amount: Double,
    @Column(name = "currency", nullable = false)
    val currency: String,
    @Column(name = "payment_method", nullable = false)
    val paymentMethod: String,
    @Column(nullable = false)
    var status: OrderStatus,
    @Column(nullable = true)
    var failureReason: String? = null,
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime,
    @OneToMany(
        mappedBy = "order",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    val items: MutableList<OrderItem> = mutableListOf(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
)
