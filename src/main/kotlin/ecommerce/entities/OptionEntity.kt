package ecommerce.entities

import ecommerce.exception.InsufficientStockException
import ecommerce.exception.InvalidOptionNameException
import ecommerce.exception.InvalidOptionQuantityException
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "`option`",
    uniqueConstraints = [UniqueConstraint(columnNames = ["product_id", "name"])],
)
class OptionEntity(
    name: String,
    quantity: Int,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    var product: ProductEntity,
    @Column(nullable = false)
    var unitPrice: Double,
    @OneToMany(mappedBy = "option", cascade = [CascadeType.ALL], orphanRemoval = true)
    val cartItems: MutableSet<CartItemEntity> = mutableSetOf(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    @Column(name = "name", nullable = false, length = 50)
    var name: String = name
        set(value) {
            validateName(value)
            field = value
        }

    @Column(name = "quantity", nullable = false)
    var quantity: Int = quantity
        set(value) {
            validateQuantity(value)
            field = value
        }

    init {
        this.name = name
        this.quantity = quantity
    }

    val totalPrice: Double
        get() = unitPrice * quantity

    fun checkStock(quantity: Int) {
        if (this.quantity == 0) throw InsufficientStockException("Option is out of stock")
        if (this.quantity < quantity) throw InsufficientStockException("Not enough stock")
    }

    fun subtract(quantity: Int) {
        if (quantity < 1) throw InvalidOptionQuantityException("Subtract amount must be >= 1")
        if (this.quantity < quantity) throw InsufficientStockException("Not enough stock")
        this.quantity -= quantity
    }

    fun validateStock(quantity: Int) {
        if (this.quantity < quantity) throw InsufficientStockException("Not enough stock for option $id")
    }

    override fun equals(other: Any?): Boolean {
        if (this == other) return true
        if (other !is OptionEntity) return false
        return product == other.product && name == other.name
    }

    override fun hashCode(): Int {
        var result = product.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    private fun validateName(name: String) {
        if (name.length > 50) throw InvalidOptionNameException("Option name too long")
        val allowed = Regex("^[\\p{Alnum} ()\\[\\]+\\-&/_]+$")
        if (!allowed.matches(name)) throw InvalidOptionNameException("Option names contains invalid characters: '$name'")
    }

    private fun validateQuantity(quantity: Int) {
        if (quantity < 1 || quantity >= 100_000_000) throw InvalidOptionQuantityException("Quantity must be between 1 and 99,999,999")
    }
}
