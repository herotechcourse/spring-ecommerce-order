package ecommerce.repository

import ecommerce.model.Product
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@JdbcTest
class ProductRepositoryTest {
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setUp() {
        productRepository = ProductRepository(jdbcTemplate)
        jdbcTemplate.execute("DROP TABLE IF EXISTS cart_items")
        jdbcTemplate.execute("DROP TABLE IF EXISTS members")
        jdbcTemplate.execute("DROP TABLE IF EXISTS products")

        createQuery().split(";")
            .filter { it.isNotBlank() }
            .forEach { jdbcTemplate.execute(it.trim()) }

        insertTestProducts()
    }

    private fun insertTestProducts() {
        val products = listOf(
            Product(0, "Coffee", 10.99, "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Roasted_coffee_beans.jpg/1200px-Roasted_coffee_beans.jpg"),
            Product(0, "Hand cream", 6.99, "https://cdn.idealo.com/folder/Product/6178/3/6178302/s1_produktbild_gross/neutrogena-hydro-boost-creme-gel-50ml.jpg"),
            Product(0, "Chair", 77.99, "https://encrypted-tbn1.gstatic.com/shopping?q=tbn:ANd9GcRqUHblIQ5QCehPdDXFHqSqUKqxNExCjw_mKe-wsYJYNXP7S7pnlrGpBKEPGzutl3CSIxRQSJjb3HbbQevqNeDMZQdxtP0ml591k0zsn-b-4KAMs9aMwJngvHNPGf7KHbNhUuOn6g&usqp=CAc"),
            Product(0, "Notebook", 3.49, "https://upload.wikimedia.org/wikipedia/commons/6/6e/Moleskine_notebook.jpg")
        )

        val insertSql = "INSERT INTO products (name, price, image_url) VALUES (?, ?, ?)"
        products.forEach {
            jdbcTemplate.update(insertSql, it.name, it.price, it.imageUrl)
        }
    }

    @Test
    fun getAll() {
        val products = productRepository.getAll()
        assertThat(products).hasSize(4)
    }

    @Test
    fun findById() {
        val product = productRepository.findById(1)
        assertThat(product?.name).isEqualTo("Coffee")
    }

    @Test
    fun update() {
        val product = Product(1, "Updated Name", 99.0, "https://example.com/image.jpg")
        productRepository.updateProduct(product)

        val updated = productRepository.findById(1)
        assertThat(updated?.name).isEqualTo("Updated Name")
        assertThat(updated?.price).isEqualTo(99.0)
        assertThat(updated?.imageUrl).isEqualTo("https://example.com/image.jpg")
    }

    @Test
    fun delete() {
        productRepository.deleteProduct(1)
        val products = productRepository.getAll()
        assertThat(products).hasSize(3)
    }

    @Test
    fun create() {
        val product = Product(0, "New Product", 11.00, "https://example.com/new.jpg")
        productRepository.createProduct(product)
        val products = productRepository.getAll()
        assertThat(products).hasSize(5)
    }

    private fun createQuery(): String {
        return """
            CREATE TABLE members (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(100),
                email VARCHAR(255) NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL,
                role VARCHAR(20) DEFAULT 'USER'
            );

            CREATE TABLE products (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(100) NOT NULL,
                price DOUBLE NOT NULL,
                image_url VARCHAR(500)
            );

            CREATE TABLE cart_items (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                member_id BIGINT NOT NULL,
                product_id BIGINT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (member_id) REFERENCES members(id),
                FOREIGN KEY (product_id) REFERENCES products(id)
            );
            """.trimIndent()
    }

    private fun createData(): String {
        return """
            INSERT INTO products (name, price, image_url) VALUES ('Coffee', 10.99, 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Roasted_coffee_beans.jpg/1200px-Roasted_coffee_beans.jpg');
            INSERT INTO products (name, price, image_url) VALUES ('Hand cream', 6.99, 'https://cdn.idealo.com/folder/Product/6178/3/6178302/s1_produktbild_gross/neutrogena-hydro-boost-creme-gel-50ml.jpg');
            INSERT INTO products (name, price, image_url) VALUES ('Chair', 77.99, 'https://encrypted-tbn1.gstatic.com/shopping?q=tbn:ANd9GcRqUHblIQ5QCehPdDXFHqSqUKqxNExCjw_mKe-wsYJYNXP7S7pnlrGpBKEPGzutl3CSIxRQSJjb3HbbQevqNeDMZQdxtP0ml591k0zsn-b-4KAMs9aMwJngvHNPGf7KHbNhUuOn6g&usqp=CAc');
            INSERT INTO products (name, price, image_url) VALUES ('Notebook', 3.49, 'https://upload.wikimedia.org/wikipedia/commons/6/6e/Moleskine_notebook.jpg');
            """.trimIndent()
    }
}
