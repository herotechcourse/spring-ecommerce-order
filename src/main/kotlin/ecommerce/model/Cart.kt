package ecommerce.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "carts")
class Cart(
    @OneToMany(cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "cart_id", referencedColumnName = "id")
    var cartItems: Set<CartItem> = emptySet(),
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
)
