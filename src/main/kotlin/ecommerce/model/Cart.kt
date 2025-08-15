package ecommerce.model

import ecommerce.utils.exception.EntityNotFoundException
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import kotlin.collections.minusAssign
import kotlin.collections.plusAssign
import kotlin.compareTo

@Entity
class Cart private constructor() {
    @OneToOne
    @JoinColumn(name = "user_id")
    lateinit var user: User

    @OneToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "cart_id")
    val items: MutableList<CartProduct> = mutableListOf()

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    constructor(user: User) : this() {
        this.user = user
    }

    fun addProduct(
        option: Option,
        quantity: Int = 1,
    ): CartProduct {
        val existing = findProduct(option)
        return if (existing != null) {
            existing.incrementQuantity(quantity)
            existing
        } else {
            val newItem = CartProduct(option, quantity)
            items.add(newItem)
            newItem
        }
    }

    fun decrementProduct(
        option: Option,
        decrement: Int = 1,
    ) {
        require(decrement > 0) { "Quantity to decrement must be greater than 0" }

        val existing =
            findProduct(option)
                ?: throw EntityNotFoundException("Product option with id ${option.id} not found")
        if (existing.quantity > decrement) {
            existing.decrementQuantity(decrement)
        } else {
            items.remove(existing)
        }
    }

    fun clear() {
        items.clear()
    }

    private fun findProduct(option: Option): CartProduct? {
        return items.find { it.option == option }
    }
}
