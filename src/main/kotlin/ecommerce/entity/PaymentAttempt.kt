package ecommerce.entity

import ecommerce.entity.enumerated.PaymentAttemptStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "payment_attempts")
class PaymentAttempt(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentAttemptStatus = PaymentAttemptStatus.PENDING,

    @Column(name = "external_id")
    var externalId: String? = null,

    @Column
    var failureCode: String? = null,

    @Column(length = 2048)
    var failureMessage: String? = null,

    @Column(nullable = false)
    val provider: String = "stripe",

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
)
