package ecommerce.model

import ecommerce.dto.LoginForm
import ecommerce.dto.MemberResponse
import ecommerce.dto.RegisterForm
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "members")
class Member(
    @Column(nullable = false, unique = true)
    val email: String,
    @Column(nullable = false)
    val password: String,
    val role: String? = null,
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
    @JoinColumn
    var cart: Cart = Cart(),
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    companion object {
        fun toEntity(
            member: Member,
            id: Long,
        ): Member {
            return Member(member.email, member.password, id = id)
        }

        fun from(loginForm: LoginForm): Member {
            return Member(email = loginForm.email, password = loginForm.password)
        }

        fun from(registerForm: RegisterForm): Member {
            return Member(email = registerForm.email, password = registerForm.password)
        }

        fun toResponse(entity: Member): MemberResponse {
            val id = entity.id
            return MemberResponse(id = id, email = entity.email)
        }
    }
}
