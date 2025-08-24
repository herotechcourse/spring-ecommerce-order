package ecommerce.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.Positive
import java.time.LocalDateTime

@Entity
@Table(name = "cart_item")
class CartItemEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    lateinit var cart: CartEntity

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    lateinit var product: ProductEntity

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_option_id", nullable = false)
    lateinit var productOption: OptionEntity

    @field:Positive
    @Column(nullable = false)
    var quantity: Int = 0

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    constructor(
        cart: CartEntity,
        product: ProductEntity,
        productOption: OptionEntity,
        quantity: Int,
        createdAt: LocalDateTime = LocalDateTime.now(),
    ) : this() {
        this.cart = cart
        this.product = product
        this.productOption = productOption
        this.quantity = quantity
        this.createdAt = createdAt
    }
}
