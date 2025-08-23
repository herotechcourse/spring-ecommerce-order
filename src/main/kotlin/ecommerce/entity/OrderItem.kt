package ecommerce.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "order_items")
class OrderItem(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,
    @Column(nullable = false)
    val productId: Long,
    @Column(nullable = false)
    val optionId: Long,
    @Column(nullable = false)
    val productNameSnapshot: ProductName,
    @Column(nullable = false)
    val priceSnapshot: Int,
    @Column(nullable = false)
    val quantity: Int,
    @Column(nullable = false)
    val subtotal: Int = priceSnapshot * quantity,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
)
