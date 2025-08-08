package ecommerce.entity

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MemberTest {
    @Test
    fun `should create member with valid email and password`() {
        val member =
            Member(
                email = "user@example.com",
                password = "securePass123",
            )

        Assertions.assertThat(member.email).isEqualTo("user@example.com")
        Assertions.assertThat(member.password).isEqualTo("securePass123")
    }

    @Test
    fun `should throw exception when email is blank`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                Member(
                    email = "   ",
                    password = "1234",
                )
            }
        Assertions.assertThat(exception.message).isEqualTo("Email cannot be blank")
    }

    @Test
    fun `should throw exception when password is blank`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                Member(
                    email = "user@example.com",
                    password = "  ",
                )
            }
        Assertions.assertThat(exception.message).isEqualTo("Password cannot be blank")
    }

    @Test
    fun `should throw exception when email format is invalid`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                Member(
                    email = "user_at_example.com",
                    password = "1234",
                )
            }
        Assertions.assertThat(exception.message).isEqualTo("Invalid email format: user_at_example.com")
    }

    @Test
    fun `should throw exception when password is not valid`() {
        val member =
            Member(
                email = "user@example.com",
                password = "securePass123",
            )

        assertThrows<IllegalArgumentException> {
            member.validatePassword("securePass")
        }
    }
}
