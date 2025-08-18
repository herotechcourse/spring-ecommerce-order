package ecommerce.entity

import ecommerce.enums.OrderAndPaymentStatus
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
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    var member: MemberEntity? = null,
    @Column
    var status: OrderAndPaymentStatus = OrderAndPaymentStatus.PENDING,
    @Column
    val createdAt: Instant = Instant.now(),
    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val orderItems: MutableList<OrderItem> = mutableListOf(),
    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
    var payment: Payment? = null,
) {
    protected constructor() : this(
        id = 0L,
        member = null,
        status = OrderAndPaymentStatus.PENDING,
        createdAt = Instant.now(),
        orderItems = mutableListOf(),
    )
}
