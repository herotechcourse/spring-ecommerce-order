package ecommerce.model

import ecommerce.exception.InsufficientQuantityException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
class Option(
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    var quantity: Int,
    @ManyToOne
    @JoinColumn(name = "product_id")
    var product: Product? = null,
    @CreationTimestamp
    var createdAt: LocalDateTime? = null,
    @UpdateTimestamp
    var lastUpdatedAt: LocalDateTime? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    init {
        require(quantity >= 0) { "Quantity cannot be negative" }
    }

    fun reduceOptionQuantity(value: Int) {
        if (quantity < value) {
            throw InsufficientQuantityException("Insufficient quantity of ${product!!.name}. Items available in stock: $quantity")
        }
        quantity -= value
    }

    fun checkAvailabilityInStock(value: Int) {
        if (quantity < value) {
            throw InsufficientQuantityException("Insufficient quantity of ${product!!.name}. Items available in stock: $quantity")
        }
    }
}
