package ecommerce.controller.member.usecase

import ecommerce.dto.MemberRegisterDTO
import ecommerce.model.Member

interface CrudMemberUseCase {
    fun findAll(): List<Member>

    fun findById(id: Long): Member

    fun findByEmail(email: String): Member

    fun save(memberRegisterDTO: MemberRegisterDTO): Member

    fun deleteAll()

    fun validateEmailUniqueness(email: String)
}
