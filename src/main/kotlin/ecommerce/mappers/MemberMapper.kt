package ecommerce.mappers

import ecommerce.dto.MemberLoginDTO
import ecommerce.dto.MemberRegisterDTO
import ecommerce.entities.MemberEntity
import ecommerce.model.Member

fun MemberEntity.toDTO() = Member(name, email, password, role, id)

fun Member.toLoginDTO() = MemberLoginDTO(id)

fun Member.toEntity() = MemberEntity(name, email, password, role, id = id)

fun MemberRegisterDTO.toEntity() = MemberEntity(name, email, password)
