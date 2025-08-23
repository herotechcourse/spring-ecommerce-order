package ecommerce.service

import ecommerce.auth.JwtTokenProvider
import ecommerce.dto.MemberRequest
import ecommerce.entity.Member
import ecommerce.repository.MemberJpaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class MemberServiceUnitTest {
    @Mock
    private lateinit var memberRepository: MemberJpaRepository

    @Mock
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @InjectMocks
    private lateinit var memberService: MemberService

    @Test
    fun `register() should save the user and return a token if the email is not taken`() {
        val request = MemberRequest("test@test.com", "password123")
        val savedMember = Member(id = 1L, email = request.email, password = request.password)

        whenever(memberRepository.findByEmail(request.email)).thenReturn(null)
        whenever(memberRepository.save(any())).thenReturn(savedMember)
        whenever(jwtTokenProvider.createToken(savedMember.id)).thenReturn("test-jwt-token")

        val response = memberService.register(request)

        assertNotNull(response)
        assertEquals("test-jwt-token", response.token)

        verify(memberRepository, times(1)).save(any())
    }

    @Test
    fun `register() should throw if email is already exists`() {
        val request = MemberRequest("test@test.com", "password123")
        val existingMember = Member(id = 1L, email = request.email, password = "some-password")

        whenever(memberRepository.findByEmail(request.email)).thenReturn(existingMember)

        val exception =
            assertThrows<IllegalArgumentException> {
                memberService.register(request)
            }
        assertEquals("Email already in use", exception.message)

        verify(memberRepository, never()).save(any())
    }

    @Test
    fun `login() should return token if user exists`() {
        val request = MemberRequest("test@test.com", "password123")
        val member = Member(id = 1L, email = request.email, password = request.password)

        whenever(memberRepository.findByEmail(request.email)).thenReturn(member)
        whenever(jwtTokenProvider.createToken(member.id)).thenReturn("test-jwt-token")

        val response = memberService.login(request)

        assertEquals("test-jwt-token", response.token)
    }

    @Test
    fun `login() should throw if user is not found`() {
        val request = MemberRequest("notfound@test.com", "password123")
        whenever(memberRepository.findByEmail(request.email)).thenReturn(null)

        val exception =
            assertThrows<IllegalArgumentException> {
                memberService.login(request)
            }
        assertEquals("Invalid email or password", exception.message)
    }

    @Test
    fun `login() should throw if password is wrong`() {
        val request = MemberRequest("test@test.com", "wrong-password")
        val member = Member(id = 1L, email = request.email, password = "correct-password")

        whenever(memberRepository.findByEmail(request.email)).thenReturn(member)

        assertThrows<IllegalArgumentException> {
            memberService.login(request)
        }
    }

    @Test
    fun `findById() return user if it exists`() {
        val memberId = 1L
        val member = Member(id = memberId, email = "test@test.com", password = "password")
        whenever(memberRepository.findById(memberId)).thenReturn(Optional.of(member))

        val foundMember = memberService.findById(memberId)

        assertEquals(memberId, foundMember.id)
    }

    @Test
    fun `findById() should throw if user is not found`() {
        val memberId = 99L
        whenever(memberRepository.findById(memberId)).thenReturn(Optional.empty())

        assertThrows<NoSuchElementException> {
            memberService.findById(memberId)
        }
    }
}
