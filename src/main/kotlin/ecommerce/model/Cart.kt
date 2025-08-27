package ecommerce.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne

@Entity
data class Cart(
    @OneToOne(cascade = [CascadeType.PERSIST])
    val member: Member,
    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "cart", orphanRemoval = true)
    val cartProducts: MutableList<CartItem> = mutableListOf(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    fun addOrUpdateCartItem(cartItem: CartItem) {
        val presentProduct =
            cartProducts.firstOrNull { it.cart.id == cartItem.cart.id && it.product.id == cartItem.product.id }
        when (presentProduct) {
            null -> cartProducts.add(cartItem)
            else -> presentProduct.quantity = cartItem.quantity
        }
    }

    fun deleteCartProduct(productId: Long): Boolean {
        return cartProducts.removeIf { it.product.id == productId }
    }

    fun clearCartProducts(): Boolean {
        val isNotEmpty = cartProducts.isNotEmpty()
        cartProducts.clear()
        return isNotEmpty
    }
}
