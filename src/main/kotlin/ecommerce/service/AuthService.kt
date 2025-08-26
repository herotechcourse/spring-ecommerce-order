package ecommerce.service

import ecommerce.auth.JwtTokenProvider
import ecommerce.dto.auth.AuthResponse
import ecommerce.dto.member.LoginForm
import ecommerce.dto.member.RegisterForm
import ecommerce.exception.AuthorizationException
import ecommerce.exception.InternalServerErrorException
import ecommerce.exception.MemberEmailAlreadyExistsException
import ecommerce.model.Cart
import ecommerce.model.Member
import ecommerce.repository.CartRepository
import ecommerce.repository.MemberRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val memberRepository: MemberRepository,
    private val cartRepository: CartRepository,
    private val jwtTokenProvider: JwtTokenProvider,
) {
    fun registerMember(form: RegisterForm): Member {
        checkMemberEmailExists(form.email)
        val registeredMember = RegisterForm(form.email, form.password)
        val member = Member(registeredMember.email, form.password)
        val savedMember = memberRepository.save(member)
        savedMember.cart = cartRepository.save(Cart())
        return memberRepository.findByIdOrNull(savedMember.id)
            ?: throw InternalServerErrorException(MESSAGE_MEMBER_NOT_FOUND)
    }

    fun loginMember(form: LoginForm): AuthResponse {
        val member = memberRepository.findByEmail(form.email) ?: throw AuthorizationException(MESSAGE_INVALID_EMAIL)
        if (member.password != form.password) throw AuthorizationException(MESSAGE_INVALID_PASSWORD)
        val accessToken = jwtTokenProvider.createToken(member.email)
        return AuthResponse(accessToken)
    }

    fun findMemberById(id: Long): Member? = memberRepository.findByIdOrNull(id)

    fun findMemberByEmail(email: String): Member? =
        memberRepository.findByEmail(email) ?: throw AuthorizationException(MESSAGE_INVALID_EMAIL)

    fun findMemberByToken(token: String): Member {
        if (!jwtTokenProvider.validateToken(token)) {
            throw AuthorizationException(MESSAGE_INVALID_TOKEN)
        }
        val email = jwtTokenProvider.getPayload(token)
        val member = memberRepository.findByEmail(email) ?: throw AuthorizationException(MESSAGE_INVALID_EMAIL)
        return member
    }

    private fun checkMemberEmailExists(
        email: String,
        originalEmail: String? = null,
    ) {
        if (originalEmail != null && email == originalEmail) {
            return
        } else if (memberRepository.findByEmail(email) != null) {
            throw MemberEmailAlreadyExistsException(MESSAGE_EMAIL_ALREADY_EXISTS)
        }
    }

    companion object {
        const val MESSAGE_MEMBER_NOT_FOUND = "Member not found"
        const val MESSAGE_INVALID_EMAIL = "Invalid email"
        const val MESSAGE_INVALID_PASSWORD = "Invalid password"
        const val MESSAGE_INVALID_TOKEN = "Invalid token"
        const val MESSAGE_EMAIL_ALREADY_EXISTS = "Email already exists"
    }
}
