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
@Table(name = "product_option")
class Option(
    name: String,
    @Column(nullable = false)
    var quantity: Int,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    @Column(name = "name", nullable = false, length = 50)
    val name = OptionName(name)

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    lateinit var product: Product

    init {
        require(quantity in 1 until 100_000_000) {
            "Quantity must be between 1 and 99,999,999."
        }
    }

    fun subtract(amount: Int) {
        if (amount <= 0) throw IllegalArgumentException("Amount must be positive.")
        if (quantity - amount < 0) throw IllegalStateException("Not enough quantity.")
        quantity -= amount
    }
}
