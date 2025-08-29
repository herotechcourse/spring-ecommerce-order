package ecommerce.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "products")
class Product(
    @Column(name = "name", nullable = false)
    var name: String,
    @Column(name = "price", nullable = false)
    var price: Double,
    @Column(name = "quantity", nullable = false)
    var quantity: Int,
    @Column(name = "imageUrl", nullable = false)
    var imageUrl: String,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Product) return false

        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    fun updateProduct(
        newName: String,
        newPrice: Double,
        newQuantity: Int,
        newImageUrl: String,
    ) {
        this.name = newName
        this.price = newPrice
        this.quantity = newQuantity
        this.imageUrl = newImageUrl
    }

    fun patchUpdate(
        newName: String? = null,
        newPrice: Double? = null,
        newQuantity: Int? = null,
        newImageUrl: String? = null,
    ) {
        newName?.let { this.name = it }
        newPrice?.let { this.price = it }
        newQuantity?.let { this.quantity = it }
        newImageUrl?.let { this.imageUrl = it }
    }

    override fun toString(): String {
        return "Product(id=$id, name=$name, price=$price, quantity=$quantity)"
    }
}
