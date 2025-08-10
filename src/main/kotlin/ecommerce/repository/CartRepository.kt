package ecommerce.repository

import ecommerce.dto.CartItem
import ecommerce.dto.MemberResponse
import ecommerce.dto.TopProductStatResponse
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.Timestamp

@Repository
class CartRepository(private val jdbcTemplate: JdbcTemplate) {
    fun add(
        memberId: Long,
        productId: Long,
    ) {
        val sql =
            """
            INSERT INTO CART (member_id, product_id, quantity, created_at)
            VALUES (?, ?, 1, ?)
            ON DUPLICATE KEY UPDATE quantity = quantity + 1
            """.trimIndent()
        val now = Timestamp(System.currentTimeMillis())

        jdbcTemplate.update(sql, memberId, productId, now)
    }

    fun remove(
        memberId: Long,
        productId: Long,
    ) {
        val sql = "DELETE FROM CART WHERE member_id = ? AND product_id = ?"

        jdbcTemplate.update(sql, memberId, productId)
    }

    fun getCartItems(memberId: Long): List<CartItem> {
        val sql =
            """
            SELECT c.product_id, p.name, p.price, c.quantity
            FROM CART c
            JOIN PRODUCTS p ON c.product_id = p.id
            WHERE c.member_id = ?
            """.trimIndent()

        return jdbcTemplate.query(sql, { rs, _ ->
            CartItem(
                productId = rs.getLong("product_id"),
                name = rs.getString("name"),
                price = rs.getDouble("price"),
                quantity = rs.getInt("quantity"),
            )
        }, memberId)
    }

    fun findTop5ProductsInLast30Days(): List<TopProductStatResponse> {
        val sql =
            """
            SELECT 
                p.product_name,
                COUNT(*) AS times_added,
                MAX(c.created_at) AS most_recent_added_time
            FROM carts c
            JOIN products p ON c.product_id = p.id
            WHERE c.created_at >= CURRENT_DATE - INTERVAL 30 DAY
            GROUP BY c.product_id, p.product_name
            ORDER BY times_added DESC, most_recent_added_time DESC
            LIMIT 5
            """.trimIndent()

        return jdbcTemplate.query(sql) { rs, _ ->
            TopProductStatResponse(
                name = rs.getString("product_name"),
                count = rs.getLong("times_added"),
                lastAddedAt = rs.getTimestamp("most_recent_added_time").toLocalDateTime(),
            )
        }
    }

    fun findMembersWithCartActivityInLast7Days(): List<MemberResponse> {
        val sql =
            """
            SELECT DISTINCT m.id, m.name, m.email
            FROM members m
            JOIN carts c ON m.id = c.member_id
            WHERE c.created_at >= NOW() - INTERVAL 7 DAY
            """.trimIndent()

        return jdbcTemplate.query(sql) { rs, _ ->
            MemberResponse(
                id = rs.getLong("id"),
                name = rs.getString("name"),
                email = rs.getString("email"),
                role = rs.getString("role"),
            )
        }
    }
}
