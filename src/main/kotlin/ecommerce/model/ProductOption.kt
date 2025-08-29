package ecommerce.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.Objects

@Entity
@Table(name = "product_options")
class ProductOption(
    @Column(name = "name", nullable = false)
    var name: String,
    @Column(name = "quantity", nullable = false)
    var quantity: Int,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product,
    @Column(name = "price", nullable = false)
    var price: Double,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    fun subtract(quantity: Int) {
        if (quantity < 1) throw IllegalArgumentException("Quantity must be greater than 0")
        if (quantity > this.quantity) throw IllegalArgumentException("Quantity can not be greater than stock of product quantity")
        this.quantity -= quantity
    }

    fun updateProductOption(
        newName: String,
        newQuantity: Int,
        newProduct: Product,
    ) {
        this.name = newName
        this.quantity = newQuantity
        this.product = newProduct
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductOption) return false

        if (id != null && other.id != null) {
            return id == other.id
        }

        return product == other.product && name == other.name
    }

    override fun hashCode(): Int {
        return Objects.hash(product.id, name)
    }

    override fun toString(): String {
        return "ProductOption(id=$id, name=$name, quantity=$quantity, productId=${product.id})"
    }
}
