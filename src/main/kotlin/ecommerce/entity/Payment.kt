package ecommerce.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "payment")
class Payment(
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    val order: Order,

    @Column(name = "intent_id", nullable = false)
    val intentId: String,

    @Column(nullable = false)
    val status: String,

    @Column(nullable = false)
    val amount: Long,

    @Column(nullable = false, length = 10)
    val currency: String,

    @Column(name = "failure_code")
    val failureCode: String? = null,

    @Column(name = "failure_message")
    val failureMessage: String? = null,

    @Column(name = "paid_at")
    val paidAt: LocalDateTime? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
)