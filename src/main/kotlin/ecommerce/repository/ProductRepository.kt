package ecommerce.repository

import ecommerce.model.Product
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class ProductRepository(private val jdbcTemplate: JdbcTemplate) {
    private val productRowMapper =
        RowMapper<Product> { rs: ResultSet, _ ->
            Product(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getString("image_url"),
            )
        }

    fun findById(id: Long): Product? {
        return jdbcTemplate.queryForObject(
            "SELECT id, name, price, image_url FROM products WHERE id = ?",
            productRowMapper,
            id,
        )
    }

    fun getAll(): List<Product> {
        return jdbcTemplate.query(
            "SELECT id, name, price, image_url FROM products",
            productRowMapper,
        )
    }

    fun createProduct(product: Product) {
        jdbcTemplate.update(
            "INSERT INTO products (name, price, image_url) VALUES (?, ?, ?)",
            product.name,
            product.price,
            product.imageUrl,
        )
    }

    fun updateProduct(product: Product) {
        jdbcTemplate.update(
            "UPDATE products SET name = ?, price = ?, image_url = ? WHERE id = ?",
            product.name,
            product.price,
            product.imageUrl,
            product.id,
        )
    }

    fun deleteProduct(id: Long) {
        jdbcTemplate.update(
            "DELETE FROM products WHERE id = ?",
            id,
        )
    }

    fun existsByName(name: String): Boolean {
        val count =
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products WHERE name = ?",
                Int::class.java,
                name,
            )
        return count != null && count > 0
    }

    fun existsByNameExcludingId(
        name: String,
        excludedId: Long,
    ): Boolean {
        val count =
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products WHERE name = ? AND id != ?",
                Int::class.java,
                name,
                excludedId,
            )
        return count != null && count > 0
    }
}
