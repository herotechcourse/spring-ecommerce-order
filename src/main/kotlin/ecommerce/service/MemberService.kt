package ecommerce.service

import ecommerce.auth.JwtTokenProvider
import ecommerce.dto.MemberRequest
import ecommerce.dto.TokenMemberResponse
import ecommerce.entity.Member
import ecommerce.repository.MemberJpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberService(
    private val memberRepository: MemberJpaRepository,
    private val jwtTokenProvider: JwtTokenProvider,
) {
    @Transactional
    fun register(request: MemberRequest): TokenMemberResponse {
        val existing = memberRepository.findByEmail(request.email)
        if (existing != null) {
            throw IllegalArgumentException("Email already in use")
        }

        val saved = memberRepository.save(Member(email = request.email, password = request.password))
        val token = jwtTokenProvider.createToken(saved.id)
        return TokenMemberResponse(token)
    }

    @Transactional(readOnly = true)
    fun login(request: MemberRequest): TokenMemberResponse {
        val member =
            memberRepository.findByEmail(request.email)
                ?: throw IllegalArgumentException("Invalid email or password")

        if (member.password != request.password) {
            throw IllegalArgumentException("Invalid email or password")
        }

        val token = jwtTokenProvider.createToken(member.id)
        return TokenMemberResponse(token)
    }

    @Transactional(readOnly = true)
    fun findByToken(token: String): Member? {
        val memberId =
            jwtTokenProvider.getSubject(token).toLongOrNull()
                ?: throw IllegalArgumentException("Invalid token")

        return memberRepository.findById(memberId).orElse(null)
    }
}
