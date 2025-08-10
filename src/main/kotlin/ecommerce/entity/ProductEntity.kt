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
@Table(name = "product")
class ProductEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true)
    var name: String,
    @Column(nullable = false)
    var price: Double,
    @Column(nullable = false, name = "image_url")
    var imageUrl: String,
    @OneToMany(
        cascade = [CascadeType.ALL],
        mappedBy = "product",
    )
    val options: MutableList<OptionEntity> = mutableListOf(),
) {
    init {
        require(name.isNotBlank()) { "Product name must not be blank" }
        require(price >= 0.01) { "Price must be positive" }
        require(imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            "Image URL must start with http:// or https://"
        }
        require(options.isNotEmpty()) { "A product must have at least one option" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    protected constructor() : this(
        name = "default",
        price = 0.01,
        imageUrl = "http://example.com/default.jpg",
        options = mutableListOf(OptionEntity(name = "defaultOption", quantity = 1)),
    )
}
