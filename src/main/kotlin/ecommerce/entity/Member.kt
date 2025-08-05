package ecommerce.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "members")
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column(name = "name")
    val name: String = "",
    @Column(name = "email", unique = true, nullable = false)
    val email: String,
    @Column(name = "password", nullable = false)
    val password: String,
    @Column(name = "role")
    val role: String = "USER",
) {
    private constructor() : this(
        id = 0L,
        name = "",
        email = "jpa@constructor.fake",
        password = "hidden",
        role = "USER"
    )
}
