package ecommerce.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "member")
class MemberEntity(
    @Column(name = "name", nullable = false, length = 50)
    val name: String,
    @Column(name = "email", nullable = false, length = 100)
    val email: String,
    @Column(name = "password", nullable = false, length = 255)
    val password: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    val role: Role = Role.CUSTOMER,
    @OneToMany(mappedBy = "member", cascade = [CascadeType.ALL], orphanRemoval = true)
    val cartItems: MutableSet<CartItemEntity> = mutableSetOf(),
    @OneToMany(mappedBy = "member", cascade = [CascadeType.ALL], orphanRemoval = true)
    val wishItems: MutableSet<WishItemEntity> = mutableSetOf(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    enum class Role { CUSTOMER, ADMIN }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MemberEntity) return false
        if (id == 0L || other.id == 0L) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
