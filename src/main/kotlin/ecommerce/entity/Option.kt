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
    @Column(name = "name", nullable = false, length = 50)
    var name: String,
    @Column(nullable = false)
    var quantity: Int,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    init {
        require(name.length <= 50) { "Option name must be 50 characters or fewer." }
        require(name.matches(Regex("^[a-zA-Z0-9 ()\\[\\]+\\-&/_]*$"))) {
            "Option name contains invalid characters."
        }

        require(quantity in 1 until 100_000_000) {
            "Quantity must be between 1 and 99,999,999."
        }
    }

    fun subtract(amount: Int) {
        require(amount > 0) { "Amount must be positive." }
        check(quantity - amount >= 0) { "Not enough quantity." }
        quantity -= amount
    }
}
