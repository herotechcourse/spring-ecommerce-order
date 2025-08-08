package ecommerce.entity

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
@Table(name = "cart")
class Cart(
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
)
