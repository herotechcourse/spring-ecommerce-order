package ecommerce.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(
    name = "order_items",
    indexes = [
        Index(name = "idx_order_item_order_id", columnList = "order_id"),
    ],
)
class OrderItem(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_option_id", nullable = false)
    var productOption: ProductOption,
    @Column(name = "quantity", nullable = false)
    var quantity: Int,
    @Column(name = "unit_price", nullable = false)
    var unitPrice: Double,
    @Column(name = "total_price", nullable = false)
    var totalPrice: Double,
    // Historical tracking - store product and option names at time of order
    @Column(name = "product_name", nullable = false)
    val productName: String,
    @Column(name = "option_name", nullable = false)
    val optionName: String,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    init {
        this.totalPrice = calculateTotalPrice()
    }

    fun getTotalAmount(): Double {
        return totalPrice
    }

    fun calculateTotalPrice(): Double {
        return quantity * unitPrice
    }

    fun updateQuantity(newQuantity: Int) {
        this.quantity = newQuantity
        this.totalPrice = calculateTotalPrice()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderItem) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "OrderItem(id=$id, productName='$productName', optionName='$optionName', quantity=$quantity, unitPrice=$unitPrice)"
    }

    companion object {
        fun fromProductOption(
            productOption: ProductOption,
            quantity: Int,
            order: Order,
        ): OrderItem {
            return OrderItem(
                order = order,
                productOption = productOption,
                quantity = quantity,
                unitPrice = productOption.price,
                totalPrice = quantity * productOption.price,
                productName = productOption.product.name,
                optionName = productOption.name,
            )
        }
    }
}
