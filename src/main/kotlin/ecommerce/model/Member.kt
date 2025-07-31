package ecommerce.model

data class Member(
    val id: Long = 0,
    val name: String = "",
    val email: String,
    val password: String,
    val role: String = "USER",
)
