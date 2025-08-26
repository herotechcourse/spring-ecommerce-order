package ecommerce.dto.member

data class ActiveMemberInfo(
    val id: Long,
    val email: String,
    val name: String? = null,
)
