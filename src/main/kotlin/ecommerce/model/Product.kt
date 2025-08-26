package ecommerce.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "products")
class Product(
    @Column(nullable = false, unique = true)
    var name: String,
    @Column(nullable = false)
    var price: Double,
    @Column(nullable = false)
    var imageUrl: String,
    options: List<Option> = emptyList(),
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    @OneToMany(mappedBy = "product", cascade = [CascadeType.PERSIST])
    val options: MutableList<Option> = options.toMutableList()

    fun addOption(option: Option) {
        require(option.name.length <= 50) { "Option name must have length up to 50" }
        require(option.quantity in 1..100_000_000) { "Option quantity must be between 1 and 100,000,000" }
        require(option.name.matches(Regex("^[a-zA-Z0-9()\\[\\]+\\-&/_ ]+$"))) { "Option name must contain valid characters" }

        option.product = this
        this.options.add(option)
    }

    fun changeName(name: String) {
        this.name = name
    }

    fun changePrice(price: Double) {
        this.price = price
    }

    fun changeImageUrl(imageUrl: String) {
        this.imageUrl = imageUrl
    }
}
