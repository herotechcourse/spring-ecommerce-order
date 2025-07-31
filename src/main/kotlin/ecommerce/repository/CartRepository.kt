package ecommerce.repository

import ecommerce.dto.MemberStatsResponse
import ecommerce.dto.ProductStatResponse
import ecommerce.model.CartItem
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class CartRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    private val rowMapper =
        RowMapper<CartItem> { rs: ResultSet, _ ->
            CartItem(
                id = rs.getLong("id"),
                memberId = rs.getLong("member_id"),
                productId = rs.getLong("product_id"),
                createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            )
        }

    fun addToCart(
        memberId: Long,
        productId: Long,
    ) {
        jdbcTemplate.update(
            "INSERT INTO cart_items (member_id, product_id) VALUES (?, ?)",
            memberId,
            productId,
        )
    }

    fun getCartItems(memberId: Long): List<CartItem> {
        return jdbcTemplate.query(
            "SELECT * FROM cart_items WHERE member_id = ?",
            rowMapper,
            memberId,
        )
    }

    fun removeFromCart(
        memberId: Long,
        productId: Long,
    ) {
        jdbcTemplate.update(
            "DELETE FROM cart_items WHERE member_id = ? AND product_id = ?",
            memberId,
            productId,
        )
    }

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
                count = rs.getInt("count"),
                lastAddedAt = rs.getTimestamp("last_added_at").toLocalDateTime().toString(),
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
