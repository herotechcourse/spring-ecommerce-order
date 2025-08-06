package ecommerce.repository

import ecommerce.model.Member
import ecommerce.model.NewMember
import jakarta.validation.ValidationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository

@Repository
class MemberRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    fun insert(newMember: NewMember): Member {
        if (existsByEmail(newMember.email)) {
            throw ValidationException("Email is already registered")
        }

        val simpleJdbcInsert =
            SimpleJdbcInsert(jdbcTemplate)
                .withTableName("members")
                .usingGeneratedKeyColumns("id")

        val parameters =
            mapOf(
                "email" to newMember.email,
                "password" to newMember.password,
                "role" to newMember.role,
            )

        val id = simpleJdbcInsert.executeAndReturnKey(parameters).toLong()

        return Member(
            id = id,
            email = newMember.email,
            password = newMember.password,
            role = newMember.role,
            name = "John Doe",
        )
    }

    fun findByEmail(email: String): Member? {
        val sql = "SELECT * FROM MEMBERS WHERE email = ?"

        return jdbcTemplate.query(sql, { rs, _ ->
            Member(
                id = rs.getLong("id"),
                email = rs.getString("email"),
                password = rs.getString("password"),
                role = rs.getString("role"),
                name = rs.getString("name"),
            )
        }, email).firstOrNull()
    }

    fun existsByEmail(email: String): Boolean {
        val sql = "SELECT COUNT(*) FROM MEMBERS WHERE email = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, email) ?: 0

        return count > 0
    }
}
