package ecommerce.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * Table carts {
 *   member_id bigint [ref: > members.id]
 *   id bigint [pk, increment]
 * }
 */
@Entity
@Table(name = "carts")
class Cart(
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", unique = true)
    val member: Member,
    @OneToMany(
        mappedBy = "cart",
        cascade = [CascadeType.PERSIST, CascadeType.REMOVE],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    val items: MutableList<CartItem> = mutableListOf(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    fun addItem(
        option: Option,
        quantity: Int,
    ): CartItem {
        require(quantity > 0) { "Item quantity must be greater than zero." }
        require(option.isAvailable(quantity)) { "Item not in stock" }
        val existingItem = items.find { it.option.id == option.id }

        return existingItem?.apply{
            existingItem.quantity += quantity
            existingItem.updatedAt = LocalDateTime.now()
        } ?: createNewCartItem(option, quantity)
    }

    private fun createNewCartItem(option: Option, quantity: Int): CartItem {
        return CartItem(option = option, cart = this, quantity = quantity).also {
            items.add(it)
        }
    }

    fun removeItem(
        option: Option,
        quantity: Int,
    ) {
        require(quantity > 0) { "Item quantity must be greater than zero." }
        val existingItem = items.find { it.option.id == option.id } ?: throw IllegalArgumentException("Item not found.")
        var quantityToRemove = quantity
        if (existingItem.quantity < quantity) quantityToRemove = existingItem.quantity

        existingItem.quantity -= quantityToRemove
    }
}
