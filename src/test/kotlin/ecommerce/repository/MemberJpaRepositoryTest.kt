package ecommerce.repository

import ecommerce.entity.Member
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class MemberJpaRepositoryTest {
    @Autowired
    private lateinit var memberJpaRepository: MemberJpaRepository

    private lateinit var savedMember: Member

    @BeforeEach
    fun setUp() {
        val member =
            Member(
                email = "email@test",
                password = "password",
            )
        savedMember = memberJpaRepository.save(member)
    }

    @Test
    fun `member find by email`() {
        val member2 = memberJpaRepository.findByEmail("email@test")

        assertThat(member2?.email).isEqualTo("email@test")
    }

    @Test
    fun `member is not found by email`() {
        val member2 = memberJpaRepository.findByEmail("emmail2@test")

        assertThat(member2).isNull()
    }
}
