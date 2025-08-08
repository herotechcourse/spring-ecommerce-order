package ecommerce.auth

import io.jsonwebtoken.JwtException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JwtTokenProviderTest {
    @Autowired private lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    fun `create and validate token successfully`() {
        val email = "test@email.com"
        val token = jwtTokenProvider.createToken(email)

        assertThat(token).isNotNull()
        assertThat(token).isNotEmpty()
        assertThat(jwtTokenProvider.validateToken(token)).isTrue()
        assertThat(jwtTokenProvider.getPayload(token)).isEqualTo(email)
    }

    @Test
    fun `validateToken() - throws exception when validate invalid token`() {
        assertThrows<JwtException> { jwtTokenProvider.validateToken("some.invalid.token") }
    }

    @Test
    fun `validateToken() - throws exception when validate empty token`() {
        assertThrows<IllegalArgumentException> { jwtTokenProvider.validateToken(" ") }
    }
}
