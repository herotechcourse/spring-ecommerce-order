package ecommerce.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.Objects

@Entity
@Table(
    name = "cart_items",
    indexes = [
        Index(name = "idx_cart_item_cart_id", columnList = "cart_id"),
    ],
)
class CartItem(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = true)
    var cart: Cart,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_option_id")
    var productOption: ProductOption,
    @Column(name = "quantity", nullable = true)
    var quantity: Int,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CartItem) return false

        if (id == null || other.id == null) {
            return cart == other.cart && productOption == other.productOption
        }

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: Objects.hash(cart.id, productOption.id)
    }

    override fun toString(): String {
        return "CartItem(id=$id, cartId=${cart.id}, productOptionId=${productOption.id}, quantity=$quantity)"
    }
}
