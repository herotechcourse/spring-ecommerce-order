package ecommerce.dto

import ecommerce.entity.Member
import ecommerce.entity.enumerated.Role

class AuthenticatedMember {
    lateinit var role: Role
    var id: Long = 0L

    companion object {
        fun from(member: Member): AuthenticatedMember = AuthenticatedMember().apply {
            role = member.role
            id = member.id
        }
    }
}
