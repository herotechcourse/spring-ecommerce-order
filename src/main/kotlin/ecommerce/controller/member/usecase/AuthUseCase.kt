package ecommerce.controller.member.usecase

import ecommerce.dto.TokenRequestDTO
import ecommerce.dto.TokenResponseDTO
import ecommerce.model.Member

interface AuthUseCase {
    fun login(tokenRequestDTO: TokenRequestDTO): TokenResponseDTO

    fun checkInvalidLogin(tokenRequestDTO: TokenRequestDTO): Boolean

    fun findMemberByToken(token: String): Member

    fun createToken(memberDTO: Member): TokenResponseDTO
}
