package ecommerce.service

import ecommerce.auth.JwtTokenProvider
import ecommerce.dto.MemberRequest
import ecommerce.repository.MemberJpaRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@SpringBootTest
@Transactional
class MemberServiceIntegrationTest {
    @Autowired
    private lateinit var memberService: MemberService

    @Autowired
    private lateinit var memberRepository: MemberJpaRepository

    @MockitoBean
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @BeforeEach
    fun setup() {
        memberRepository.deleteAll()
        whenever(jwtTokenProvider.createToken(any())).thenReturn("test-jwt-token")
    }

    @Test
    fun `register() should save user in bd`() {
        val request = MemberRequest("test@test.com", "password123")

        val response = memberService.register(request)

        assertEquals("test-jwt-token", response.token)

        val foundMember = memberRepository.findByEmail(request.email)
        assertNotNull(foundMember)
        assertEquals(request.email, foundMember.email)
    }

    @Test
    fun `register() should throw if user with email already exists`() {
        memberRepository.save(ecommerce.entity.Member(email = "test@test.com", password = "some-password"))

        val request = MemberRequest("test@test.com", "password123")

        val exception =
            assertThrows<IllegalArgumentException> {
                memberService.register(request)
            }
        assertEquals("Email already in use", exception.message)
    }

    @Test
    fun `login() should return token for exist user`() {
        val request = MemberRequest("test@test.com", "password123")
        memberRepository.save(ecommerce.entity.Member(email = request.email, password = request.password))

        val response = memberService.login(request)

        assertNotNull(response)
        assertEquals("test-jwt-token", response.token)
    }

    @Test
    fun `login() should thrown if password is wrong`() {
        val request = MemberRequest("test@test.com", "wrong-password")
        memberRepository.save(ecommerce.entity.Member(email = request.email, password = "correct-password"))

        assertThrows<IllegalArgumentException> {
            memberService.login(request)
        }
    }
}
