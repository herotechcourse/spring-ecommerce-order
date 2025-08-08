package ecommerce.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "options")
class Option(
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false)
    var quantity: Int,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    @ManyToOne(optional = false)
    var product: Product? = null

    init {
        require(name.length <= 50) { "Option name must have length up to 50" }
        require(quantity in 1..100_000_000) { "Option quantity must be between 1 and 100,000,000" }
        require(name.matches(Regex("^[a-zA-Z0-9()\\[\\]+\\-&/_ ]+$"))) { "Option name must contain valid characters" }
    }

    fun subtract(quantity: Int) {
        this.quantity -= quantity
    }
}
