import ecommerce.dto.TokenRequest
import ecommerce.entity.MemberEntity
import ecommerce.handler.AuthorizationException
import ecommerce.handler.ValidationException
import ecommerce.infrastructure.JWTProvider
import ecommerce.repository.MemberRepositoryJpa
import ecommerce.service.AuthService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`


class AuthServiceTest {

    private val memberRepository = mock<MemberRepositoryJpa>()
    private val jwtTokenProvider = mock<JWTProvider>()
    private val authService = AuthService(jwtTokenProvider, memberRepository)

    @Test
    fun `createToken returns token when valid email and password`() {
        val request = TokenRequest(name= "Jon", email = "user@example.com", password = "pass123", role = "ADMIN")
        val member = MemberEntity(id = 1L, name = "User", email = request.email, password = request.password, role = "USER")

        `when`(memberRepository.findByEmail(request.email)).thenReturn(member)
        `when`(jwtTokenProvider.createToken(request.email)).thenReturn("token123")

        val response = authService.createToken(request)

        assertEquals("token123", response.accessToken)
    }

    @Test
    fun `createToken throws AuthorizationException when member not found`() {
        val request = TokenRequest(name= "Jon", email = "user@example.com", password = "pass123", role = "ADMIN")

        `when`(memberRepository.findByEmail(request.email)).thenReturn(null)

        val exception = assertThrows<AuthorizationException> {
            authService.createToken(request)
        }
        assertTrue(exception.message!!.contains("Member not found"))
    }

    @Test
    fun `createToken throws AuthorizationException when password is invalid`() {
        val request = TokenRequest(name= "Jon", email = "user@example.com", password = "pass123", role = "USER")
        val member = MemberEntity(id = 1L, name = "User", email = request.email, password = "correctpass", role = "USER")

        `when`(memberRepository.findByEmail(request.email)).thenReturn(member)

        val exception = assertThrows<AuthorizationException> {
            authService.createToken(request)
        }
        assertTrue(exception.message!!.contains("Invalid password"))
    }

    @Test
    fun `register throws ValidationException when email already exists`() {
        val request = TokenRequest(name= "Jon", email = "user@example.com", password = "pass123", role = "USER")

        `when`(memberRepository.existsByEmail(request.email)).thenReturn(true)

        val exception = assertThrows<ValidationException> {
            authService.register(request)
        }
        assertEquals("Email is already registered", exception.message)
    }

    @Test
    fun `register saves new member and returns token`() {
        val request = TokenRequest(name= "Jon", email = "user@example.com", password = "pass123", role = "USER")

        `when`(memberRepository.existsByEmail(request.email)).thenReturn(false)
        val savedMember = MemberEntity(id = 10L, name = request.name, email = request.email, password = request.password, role = "USER")
        `when`(memberRepository.save(any())).thenReturn(savedMember)
        `when`(jwtTokenProvider.createToken(request.email)).thenReturn("newtoken123")

        val response = authService.register(request)

        assertEquals("newtoken123", response.accessToken)
        verify(memberRepository).save(any())
    }

    @Test
    fun `findMemberByToken returns member response when token valid`() {
        val token = "valid.token.string"
        val email = "user@example.com"
        val member = MemberEntity(id = 5L, name = "User", email = email, password = "pass", role = "USER")

        doNothing().`when`(jwtTokenProvider).validateToken(token)
        `when`(jwtTokenProvider.getPayload(token)).thenReturn(email)
        `when`(memberRepository.findByEmail(email)).thenReturn(member)

        val response = authService.findMemberByToken(token)

        assertEquals(member.id, response.id)
        assertEquals(member.email, response.email)
        assertEquals(member.role, response.role)
        assertEquals(member.name, response.name)
    }

    @Test
    fun `findMemberByToken throws AuthorizationException when member not found`() {
        val token = "valid.token.string"
        val email = "missing@example.com"

        doNothing().`when`(jwtTokenProvider).validateToken(token)
        `when`(jwtTokenProvider.getPayload(token)).thenReturn(email)
        `when`(memberRepository.findByEmail(email)).thenReturn(null)

        val exception = assertThrows<AuthorizationException> {
            authService.findMemberByToken(token)
        }
        assertTrue(exception.message!!.contains("Member not found"))
    }
}
