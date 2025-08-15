package ecommerce.model

import ecommerce.enums.CartAction
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
class CartStatistic(
    @Column(name = "user_id", nullable = false)
    var userId: Long,
    @Column(name = "user_email", nullable = false)
    var userEmail: String,
    @Column(name = "user_name", nullable = false)
    var userName: String,
    @Column(name = "option_id", nullable = false)
    var optionId: Long,
    @Column(name = "option_name", nullable = false)
    var optionName: String,
    @Column(name = "option_price", nullable = false)
    var optionPrice: Double,
    @Column(name = "action", nullable = false)
    @Enumerated(EnumType.STRING)
    var action: CartAction,
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
)
