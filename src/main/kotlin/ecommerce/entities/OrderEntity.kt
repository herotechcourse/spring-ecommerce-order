package ecommerce.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "\"order\"")
class OrderEntity(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: OrderStatus,
    @Column(nullable = false)
    val totalAmount: Long,
    @ManyToOne(fetch = FetchType.LAZY)
    val member: MemberEntity,
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<OrderItemEntity> = mutableListOf(),
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    val payments: MutableList<PaymentEntity> = mutableListOf(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
) : Auditable() {
    enum class OrderStatus {
        CREATED,
        PAID,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELED,
        RETURNED,
    }

    fun addAllItems(orderItems: List<OrderItemEntity>) {
        items.clear()
        items.addAll(orderItems)
    }

    fun addPayment(payment: PaymentEntity) {
        payments.add(payment)
    }
}
