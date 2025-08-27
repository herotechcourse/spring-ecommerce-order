package ecommerce.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "ORDER_ITEMS")
data class OrderItem(
    @ManyToOne
    @JoinColumn(name = "order_id")
    val order: Order,
    @ManyToOne
    @JoinColumn(name = "product_id")
    val product: Product,
    @ManyToOne
    @JoinColumn(name = "option_id")
    val option: Option,
    @Column(nullable = false)
    val quantity: Int,
    @Column(nullable = false)
    val price: Double,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
)
