package ecommerce.repository

import ecommerce.model.Product
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class JdbcProductStore(private val jdbcTemplate: JdbcTemplate) : ProductStore {
    private val productRowMapper =
        RowMapper<Product> { rs: ResultSet, _ ->
            Product(
                id = rs.getLong("id"),
                name = rs.getString("product_name"),
                price = rs.getDouble("price"),
                imageUrl = rs.getString("image_url"),
            )
        }

    override fun countProducts(): Int {
        val sql = "SELECT COUNT(*) FROM products"

        return jdbcTemplate.queryForObject(sql, Int::class.java) ?: 0
    }

    override fun findAll(): List<Product> {
        val sql = "Select * from products"

        return jdbcTemplate.query(sql, productRowMapper)
    }

    override fun findById(id: Long): Product {
        val sql = "SELECT * FROM products WHERE id = ?"

        return jdbcTemplate.queryForObject(sql, productRowMapper, id)
            ?: throw NoSuchElementException("Product with id $id not found")
    }

    override fun findByName(name: String): Product? {
        val sql = "SELECT * FROM products WHERE product_name = ?"

        return jdbcTemplate.query(sql, productRowMapper, name).firstOrNull()
    }

    override fun existsByName(name: String): Boolean {
        val sql = "SELECT COUNT(*) FROM PRODUCTS WHERE product_name = ?"
        val count = jdbcTemplate.queryForObject(sql, Long::class.java, name)

        return count != null && count > 0
    }

    override fun save(product: Product): Product {
        val sql = "INSERT INTO products(product_name, price, image_url) VALUES (?, ?, ?)"
        val keyHolder = GeneratedKeyHolder()

        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, arrayOf("id"))
            ps.setString(1, product.name)
            ps.setDouble(2, product.price)
            ps.setString(3, product.imageUrl)
            ps
        }, keyHolder)

        val generatedId =
            keyHolder.key?.toLong()
                ?: throw IllegalStateException("Failed to retrieve generated product ID")

        return Product(
            id = generatedId,
            name = product.name,
            price = product.price,
            imageUrl = product.imageUrl,
        )
    }

    override fun update(
        id: Long,
        product: Product,
    ): Boolean {
        val sql = "update products set product_name = ?, price = ?, image_url = ? where id = ?"

        return jdbcTemplate.update(sql, product.name, product.price, product.imageUrl, id) == NUMBER_OF_AFFECTED_ROWS
    }

    override fun delete(id: Long): Boolean {
        val sql = "delete from products where id = ?"

        return jdbcTemplate.update(sql, id) == NUMBER_OF_AFFECTED_ROWS
    }

    companion object {
        private const val NUMBER_OF_AFFECTED_ROWS = 1
    }
}
