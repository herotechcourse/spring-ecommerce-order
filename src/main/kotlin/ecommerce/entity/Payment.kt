package ecommerce.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "payments")
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order? = null,
    @Column
    val status: String = "PENDING",
    @Column
    val createdAt: Instant = Instant.now(),
    @Column(nullable = false)
    val stripePaymentIntentId: String? = null,
    var amount: Long,
) {
    protected constructor() : this(
        id = null,
        order = null,
        status = "PENDING",
        createdAt = Instant.now(),
        stripePaymentIntentId = null,
        amount = 0,
    )
}
