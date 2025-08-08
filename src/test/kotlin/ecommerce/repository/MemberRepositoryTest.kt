package ecommerce.repository

import ecommerce.model.Member
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.repository.findByIdOrNull

@DataJpaTest
class MemberRepositoryTest(
    @Autowired private val memberRepository: MemberRepository,
    @Autowired private val testEntityManager: TestEntityManager,
) {
    @AfterEach
    fun clean() {
        testEntityManager.clear()
    }

    @Test
    fun save() {
        val member = Member(email = "test@test.com", password = "test1234")
        val savedMember = memberRepository.save(member)
        val target = memberRepository.findByIdOrNull(savedMember.id)
        assertThat(target?.email).isEqualTo(member.email)
        assertThat(target?.password).isEqualTo(member.password)
    }

    @Test
    fun findByEmail() {
        val expected = "findByEmail@htc.com"
        val member = Member(email = expected, password = "test1234")
        val savedMember = memberRepository.save(member)
        val target = memberRepository.findByEmail(expected)
        assertThat(target?.email).isEqualTo(expected)
    }

    @Test
    fun findById() {
        val expected = "test@htc.com"
        val member = Member(email = expected, password = "test1234")
        val savedMember = memberRepository.save(member)
        val target = memberRepository.findByIdOrNull(savedMember.id)
        assertThat(target?.email).isEqualTo(expected)
    }

    @Test
    fun `existsByEmail() - return false if email does not exist`() {
        val target = "test@test.com"
        val actual = memberRepository.findByEmail(target)
        assertThat(actual).isNull()
    }
}
