package ecommerce.model

data class Member(
    val id: Long,
    val email: String,
    val password: String,
    val role: String,
    val name: String,
)
