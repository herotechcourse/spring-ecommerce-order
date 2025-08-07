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
    @OneToMany(mappedBy = "product", cascade = [CascadeType.PERSIST], orphanRemoval = true)
    val options: MutableList<Option> = options.toMutableList()

    init {
        require(options.isNotEmpty()) { "Options must not be empty" }
        require(options.size == options.map { it.name }.distinct().size) { "Option names must be distinct" }
        options.forEach { it.product = this }
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

    fun addOption(option: Option) {
        require(options.size == options.map { it.name }.distinct().size)
        this.options.add(option)
        option.product = this
    }
}
