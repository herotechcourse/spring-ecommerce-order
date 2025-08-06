package ecommerce.repository

import ecommerce.entity.MemberEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class MemberRepositoryJpaTest(
    @Autowired
    private val repo: MemberRepositoryJpa,
) {
    @Test
    fun `save persists and assigns id`() {
        val sampleMemberEntity =
            MemberEntity(
                email = "alice@example.com",
                password = "secret",
                role = "USER",
                name = "Alice",
            )
        val saved = repo.save(sampleMemberEntity)
        assertThat(saved.id).isNotNull()
    }

    @Test
    fun `findById returns member when id exists`() {
        val sampleMemberEntity =
            MemberEntity(
                email = "alice@example.com",
                password = "secret",
                role = "USER",
                name = "Alice",
            )
        val saved = repo.save(sampleMemberEntity)
        val found = repo.findById(saved.id!!)
        assertThat(found).isPresent
        assertThat(found.get().name).isEqualTo("Alice")
    }

    @Test
    fun `should return all members`() {
        val sampleMemberEntity1 =
            MemberEntity(
                email = "alice@example.com",
                password = "secret",
                role = "USER",
                name = "Alice",
            )
        val sampleMemberEntity2 =
            MemberEntity(
                email = "john@example.com",
                password = "secret2",
                role = "USER2",
                name = "John",
            )
        repo.save(sampleMemberEntity1)
        repo.save(sampleMemberEntity2)

        val found = repo.findAll()
        assertThat(found)
            .isNotEmpty
            .hasSize(2)
    }

    @Test
    fun `findById should return empty when member does not exist`() {
        assertThat(repo.findById(999L)).isEmpty
    }

    @Test
    fun `findByEmail returns member when email exists`() {
        val sampleMemberEntity =
            MemberEntity(
                email = "alice@example.com",
                password = "secret",
                role = "USER",
                name = "Alice",
            )
        repo.save(sampleMemberEntity)

        val found = repo.findByEmail("alice@example.com")

        assertThat(found).isNotNull
        assertThat(found!!.email).isEqualTo("alice@example.com")
    }

    @Test
    fun `findByEmail returns null when email does not exist`() {
        assertThat(repo.findByEmail("unknown@example.com")).isNull()
    }

    @Test
    fun `existsByEmail returns true when member with that email exists`() {
        val sampleMemberEntity =
            MemberEntity(
                email = "alice@example.com",
                password = "secret",
                role = "USER",
                name = "Alice",
            )
        repo.save(sampleMemberEntity)

        assertThat(repo.existsByEmail("alice@example.com")).isTrue()
    }

    @Test
    fun `existsByEmail returns false when member with that email does not exist`() {
        assertThat(repo.existsByEmail("alice@example.com")).isFalse()
    }

    @Test
    fun `deleteById should remove the member`() {
        val saved =
            repo.save(
                MemberEntity(
                    email = "alice@example.com",
                    password = "secret",
                    role = "USER",
                    name = "Alice",
                ),
            )

        repo.deleteById(saved.id!!)

        assertThat(repo.existsById(saved.id!!)).isFalse
    }

    @Test
    fun `delete should remove the member`() {
        val saved =
            repo.save(
                MemberEntity(
                    email = "alice@example.com",
                    password = "secret",
                    role = "USER",
                    name = "Alice",
                ),
            )

        repo.delete(saved)

        assertThat(repo.existsById(saved.id!!)).isFalse
    }
}
