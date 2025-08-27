package ecommerce.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "PAYMENTS")
data class Payment(
    @OneToOne
    @JoinColumn(name = "order_id")
    val order: Order,
    @Column(name = "checkout_session_id", nullable = false, unique = true)
    val checkoutSessionId: String,
    @Column(name = "payment_intent_id", nullable = true)
    val paymentIntentId: String?,
    @Column(nullable = false)
    val amount: Int,
    @Column(nullable = false)
    val currency: String,
    @Column(nullable = false)
    val status: String,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
)
