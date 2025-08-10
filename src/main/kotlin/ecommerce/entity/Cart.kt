package ecommerce.entity

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
import java.time.LocalDateTime

@Entity
@Table(name = "carts", uniqueConstraints = [UniqueConstraint(columnNames = ["member_id"])])
class Cart(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,
    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<CartItem> = mutableListOf(),
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    fun add(
        option: Option,
        quantity: Int,
    ) {
        require(quantity > 0) { "quantity must be positive." }
        val existing = items.firstOrNull { it.option.id == option.id }
        if (existing != null) {
            existing.quantity += quantity
        } else {
            items += CartItem(this, option, quantity)
        }
        updatedAt = LocalDateTime.now()
    }

    fun remove(
        optionId: Long,
        quantity: Int,
    ) {
        require(quantity > 0) { "quantity must be positive." }
        val existing = items.firstOrNull { it.option.id == optionId } ?: throw NoSuchElementException()
        require(existing.quantity >= quantity) { "Cannot remove $quantity, only ${existing.quantity} in cart." }
        if (existing.quantity == quantity) {
            items.removeIf { it.option.id == optionId }
        } else {
            existing.quantity -= quantity
        }
        updatedAt = LocalDateTime.now()
    }
}
