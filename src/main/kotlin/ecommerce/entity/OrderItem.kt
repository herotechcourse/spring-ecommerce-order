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

@Entity
@Table(name = "order_item")
class OrderItem(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,
    @Column(name = "product_id", nullable = false)
    val productId: Long,
    @Column(name = "product_name", nullable = false)
    val productName: String,
    @Column(name = "option_id", nullable = false)
    val optionId: Long,
    @Column(name = "option_name", nullable = false)
    val optionName: String,
    @Column(name = "unit_price", nullable = false)
    val unitPrice: Long,
    @Column(nullable = false)
    val quantity: Int,
    @Column(name = "line_total", nullable = false)
    val lineTotal: Long = unitPrice * quantity,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
)
