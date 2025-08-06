package ecommerce.entity

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
    @Column(name = "name", nullable = false)
    val name: String,
    @Column(name = "price", nullable = false)
    val price: Double,
    @Column(name = "image_url")
    val imageUrl: String,
    options: List<Option>,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL])
    val options: MutableList<Option> = options.toMutableList()

    init {
        require(options.isNotEmpty()) { "At least one option must be provided" }
        options.forEach { it.product = this }
    }

    fun addOption(option: Option) {
        require(options.none { it.name == option.name })
        options.add(option)
        option.product = this
    }
}
