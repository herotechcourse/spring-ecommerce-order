package ecommerce.model

import ecommerce.enums.OrderStatus
import ecommerce.enums.PaymentOption
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import java.time.LocalDateTime

@Entity
class MemberOrder(
    @OneToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "member_orders_id", nullable = false)
    val optionProducts: List<OrderProduct> = listOf(),
    @Column(name = "user_id", nullable = false)
    var userId: Long,
    @Column(name = "user_email", nullable = false)
    var userEmail: String,
    @Column(name = "payment_id", nullable = false)
    val paymentId: String,
    @Column(name = "payment_option", nullable = false)
    @Enumerated(EnumType.STRING)
    var paymentOption: PaymentOption = PaymentOption.STRIPE,
    @Column(name = "total_amount", nullable = false)
    var totalAmount: Double,
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: OrderStatus,
    @Column(name = "attempt", nullable = false)
    var attempt: Int = 1,
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    fun incrementAttempt() {
        attempt++
    }

    fun changeStatus(status: OrderStatus) {
        this.status = status
    }
}
