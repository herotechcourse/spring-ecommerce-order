package ecommerce.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "order_item")
class OrderItem(
    @Column(nullable = false)
    val quantity: Int,
    @Column(nullable = false)
    val productName: String,
    @Column(nullable = false)
    val optionName: String,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,
    @Id
    @GeneratedValue(GenerationType.IDENTITY)
    val id: Long? = null,
)
