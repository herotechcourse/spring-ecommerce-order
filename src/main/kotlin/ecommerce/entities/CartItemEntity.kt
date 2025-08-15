package ecommerce.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    name = "cart_item",
    uniqueConstraints = [UniqueConstraint(columnNames = ["member_id", "option_id"])],
)
class CartItemEntity(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    val member: MemberEntity,
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "option_id", nullable = false)
    val option: OptionEntity,
    @Column(name = "quantity", nullable = false)
    var quantity: Int,
    @Column(name = "added_at", nullable = false)
    val addedAt: LocalDateTime,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CartItemEntity) return false
        return member == other.member && option == other.option
    }

    override fun hashCode(): Int {
        var result = member.hashCode()
        result = 31 * result + option.hashCode()
        return result
    }
}
