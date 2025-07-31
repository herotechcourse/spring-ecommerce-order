package ecommerce.repository

import ecommerce.model.Member
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.stereotype.Repository
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement

@Repository
class MemberRepository(private val jdbcTemplate: JdbcTemplate) {
    private val memberRowMapper =
        RowMapper<Member> { rs: ResultSet, _ ->
            Member(
                id = rs.getLong("id"),
                email = rs.getString("email"),
                password = rs.getString("password"),
            )
        }

    fun save(member: Member): Member {
        val sql = "INSERT INTO members (email, password) VALUES (?, ?)"
        val keyHolder: KeyHolder = GeneratedKeyHolder()

        jdbcTemplate.update({ connection ->
            val ps: PreparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setString(1, member.email)
            ps.setString(2, member.password)
            ps
        }, keyHolder)

        val generatedId = keyHolder.key?.toLong() ?: throw RuntimeException("Failed to retrieve generated ID")
        return member.copy(id = generatedId)
    }

    fun findByEmail(email: String): Member? {
        val sql = "SELECT id, email, password FROM members WHERE email = ?"
        return jdbcTemplate.query(sql, memberRowMapper, email).firstOrNull()
    }

    fun findById(id: Long): Member? {
        val sql = "SELECT id, email, password FROM members WHERE id = ?"
        return jdbcTemplate.query(sql, memberRowMapper, id).firstOrNull()
    }
}
