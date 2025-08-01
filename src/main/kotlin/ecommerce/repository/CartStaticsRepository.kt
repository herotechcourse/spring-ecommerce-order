package ecommerce.repository

import ecommerce.dto.MemberStatsResponse
import ecommerce.dto.ProductStatResponse
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class CartStaticsRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    fun getTop5MostAddedProducts(): List<ProductStatResponse> {
        val sql =
            """
            SELECT p.name, COUNT(*) AS count, MAX(c.created_at) AS last_added_at
            FROM cart c
            JOIN products p ON c.product_id = p.id
            WHERE c.created_at >= DATEADD('DAY', -30, CURRENT_DATE)
            GROUP BY p.id, p.name
            ORDER BY count DESC, last_added_at DESC
            LIMIT 5
            """.trimIndent()

        return jdbcTemplate.query(sql) { rs, _ ->
            ProductStatResponse(
                name = rs.getString("name"),
                count = rs.getLong("count"),
                lastAddedAt = rs.getTimestamp("last_added_at").toLocalDateTime(),
            )
        }
    }

    fun getRecentlyActiveMembers(): List<MemberStatsResponse> {
        val sql =
            """
            SELECT DISTINCT m.id, m.name, m.email
            FROM cart c
            JOIN members m ON c.member_id = m.id
            WHERE c.created_at >= DATEADD('DAY', -7, CURRENT_DATE)
            """.trimIndent()

        return jdbcTemplate.query(sql) { rs, _ ->
            MemberStatsResponse(
                id = rs.getLong("id"),
                name = rs.getString("name"),
                email = rs.getString("email"),
            )
        }
    }
}
