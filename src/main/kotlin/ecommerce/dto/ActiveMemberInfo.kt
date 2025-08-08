package ecommerce.dto

data class ActiveMemberInfo(
    val id: Long,
    val email: String,
    val name: String? = null,
)
