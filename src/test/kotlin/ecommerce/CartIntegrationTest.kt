package ecommerce

import ecommerce.dto.CartRequest
import ecommerce.dto.MemberRequest
import ecommerce.model.Product
import ecommerce.repository.ProductRepository
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CartIntegrationTest {
    @LocalServerPort
    var port: Int = 0

    @Autowired
    private lateinit var productRepository: ProductRepository
    private lateinit var token: String
    private lateinit var product: Product

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @BeforeEach
    fun setUp() {
        RestAssured.port = port

        jdbcTemplate.execute("DROP TABLE IF EXISTS cart_products")
        jdbcTemplate.execute("DROP TABLE IF EXISTS products")
        jdbcTemplate.execute("DROP TABLE IF EXISTS members")

        jdbcTemplate.execute(
            """
            CREATE TABLE members (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                email VARCHAR(255) NOT NULL,
                password VARCHAR(255) NOT NULL
            )
            """.trimIndent(),
        )

        jdbcTemplate.execute(
            """
            CREATE TABLE products (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                price DOUBLE NOT NULL,
                image_url VARCHAR(500)
            )
            """.trimIndent(),
        )

        jdbcTemplate.execute(
            """
            CREATE TABLE cart_items (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                member_id BIGINT NOT NULL,
                product_id BIGINT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """.trimIndent(),
        )

        val memberRequest = MemberRequest("user@example.com", "password123")

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body(memberRequest)
            .post("/api/members/register")
            .then()
            .statusCode(HttpStatus.CREATED.value())

        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(memberRequest)
                .post("/api/members/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()

        token = response.jsonPath().getString("token")

        // Create a product to add to cart
        val productObj =
            Product(
                id = 0,
                name = "Test Product",
                price = 10.0,
                imageUrl = "https://example.com/image.jpg",
            )
        productRepository.createProduct(productObj)
        product = productRepository.getAll().first()
    }

    @Test
    fun `user can add, get, and remove product in cart`() {
        val cartRequest = CartRequest(productId = product.id ?: throw IllegalStateException("Product not yet persisted"))

        // Add to cart
        RestAssured.given()
            .header("Authorization", "Bearer $token")
            .contentType(ContentType.JSON)
            .body(cartRequest)
            .post("/api/cart")
            .then()
            .statusCode(HttpStatus.OK.value())

        // Get cart
        val cartItems =
            RestAssured.given()
                .header("Authorization", "Bearer $token")
                .get("/api/cart")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList("", Map::class.java)

        assertThat(cartItems).hasSize(1)
        val expectedProductId = requireNotNull(product.id).toInt()
        assertThat(cartItems[0]["productId"]).isEqualTo(expectedProductId)

        // Remove from cart
        RestAssured.given()
            .header("Authorization", "Bearer $token")
            .contentType(ContentType.JSON)
            .body(cartRequest)
            .delete("/api/cart")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())

        // Check cart is empty again
        val updatedCartItems =
            RestAssured.given()
                .header("Authorization", "Bearer $token")
                .get("/api/cart")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList("", Map::class.java)

        assertThat(updatedCartItems).isEmpty()
    }
}
