package ecommerce.repository

import ecommerce.model.Member
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@DataJpaTest
@ActiveProfiles("test")
class MemberRepositoryTest {
    @Autowired
    private lateinit var memberRepository: MemberJpaRepository

    @Test
    fun save() {
        val user = Member("test", "test@example.com", "test123")
        memberRepository.save(user)
        val member = memberRepository.findByEmail("test@example.com")
        assertThat(member?.name).isEqualTo("test")
    }
}
