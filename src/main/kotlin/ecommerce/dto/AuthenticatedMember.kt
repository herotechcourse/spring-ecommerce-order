package ecommerce.dto

import ecommerce.entity.enumerated.Role

class AuthenticatedMember(
    val role: Role,
    var id: Long = 0L,
)
