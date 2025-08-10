package ecommerce.entity

import ecommerce.entity.enumerated.Role
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "members")
class Member(
    @Column(name = "email", unique = true, nullable = false)
    val email: String,
    @Column(name = "password", nullable = false)
    val password: String,
    @Column(name = "name")
    val name: String = "",
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    val role: Role = Role.USER,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
) {
    init {
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(password.isNotBlank()) { "Password cannot be blank" }
        require(isValidEmail(email)) { "Invalid email format: $email" }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return email.matches(emailRegex)
    }

    fun validatePassword(password: String): Boolean =
        if (password == this.password) {
            true
        } else {
            throw IllegalArgumentException("Invalid email or password")
        }
}
