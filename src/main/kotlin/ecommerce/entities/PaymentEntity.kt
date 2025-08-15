package ecommerce.entities

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

@Entity
@Table(name = "payment")
class PaymentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false, unique = true)
    val stripePaymentIntentId: String,
    @Column(nullable = false)
    val amount: Long,
    @Column(nullable = false)
    val currency: String,
    @Enumerated(EnumType.STRING)
    var status: PaymentStatus,
    val failureCode: String? = null,
    val failureMessage: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: OrderEntity,
) : Auditable() {
    enum class PaymentStatus(val value: String) {
        CANCELED("canceled"),
        PROCESSING("processing"),
        REQUIRES_ACTION("requires_action"),
        REQUIRES_CAPTURE("requires_capture"),
        REQUIRES_CONFIRMATION("requires_confirmation"),
        REQUIRES_PAYMENT_METHOD("requires_payment_method"),
        SUCCEEDED("succeeded"),
        ;

        companion object {
            fun fromValue(value: String): PaymentStatus =
                entries.find { it.value == value }
                    ?: throw IllegalArgumentException("Unknown payment status: $value")
        }
    }
}
