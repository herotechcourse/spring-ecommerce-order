package ecommerce.model

import ecommerce.dto.OrderItemResponse
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "order_items")
class OrderItem(
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member,
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product,
    @ManyToOne
    @JoinColumn(name = "option_id", nullable = false)
    var option: Option,
    @Column(nullable = false)
    var quantity: Int = 1,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    companion object {
        fun from(cartItem: CartItem): OrderItem {
            return OrderItem(
                cartItem.member,
                cartItem.product,
                cartItem.option,
                cartItem.quantity,
            )
        }

        fun to(orderItem: OrderItem): OrderItemResponse {
            return OrderItemResponse(
                itemId = orderItem.id,
                productName = orderItem.product.name,
                optionName = orderItem.option.name,
                quantity = orderItem.quantity,
                createdAt = orderItem.createdAt,
                updatedAt = orderItem.updatedAt,
            )
        }
    }
}
