package ecommerce.exception

import ecommerce.dto.OptionDto
import ecommerce.dto.ProductRequest
import ecommerce.dto.RegistrationRequest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
class ValidationTest {
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    lateinit var token: String

    @BeforeEach
    fun setUp() {
        jdbcTemplate.execute("DROP TABLE CART_HISTORY IF EXISTS")
        jdbcTemplate.execute("DROP TABLE CART_ITEMS IF EXISTS")
        jdbcTemplate.execute("DROP TABLE CARTS IF EXISTS")
        jdbcTemplate.execute("DROP TABLE PRODUCTS IF EXISTS")

        jdbcTemplate.execute(createQuery())
        val loginRequest =
            RegistrationRequest(
                "Admin Test",
                "admin@test.com",
                "12345678",
            )

        val response =
            RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post("/api/members/login")
                .then().extract()

        token = response.body().jsonPath().getString("token")
    }

    private fun createQuery(): String {
        return """
            create table PRODUCTS
            (
                ID       int              not null AUTO_INCREMENT,
                NAME     varchar(100)     not null,
                PRICE    double not null,
                IMAGE_URL varchar(500),
                PRIMARY KEY (ID)
            )
            """.trimIndent()
    }

    @Test
    fun `Should throw if name of product is blank`() {
        val optionRequest = listOf(OptionDto("option", 10))
        val product = ProductRequest("", 39.00, "http://www.test.com/test.jpg", optionRequest)
        val response =
            RestAssured
                .given().log().all().body(product)
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .`when`()
                .request("POST", "/api/admin/products")
                .then()
                .extract()
                .response()
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST.value())
        assertThat(response.body.jsonPath().get<String>("name")).contains("Name must not be blank")
    }

    @Test
    fun `Should throw if name has more then 15 letters`() {
        val optionRequest = listOf(OptionDto("option", 10))
        val product = ProductRequest("1234567890123 56", 39.00, "http://www.test.com/test.jpg", optionRequest)
        val response =
            RestAssured
                .given().log().all().body(product)
                .contentType(ContentType.JSON)
                .auth().oauth2(token)
                .`when`()
                .request("POST", "/api/admin/products")
                .then()
                .extract()
                .response()
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST.value())
        assertThat(response.body.jsonPath().get<String>("name")).contains("Name must be at most 15 characters long")
    }

    @Test
    fun `Should throw if name has invalid characters`() {
        val optionRequest = listOf(OptionDto("option", 10))
        val product = ProductRequest("?", 39.00, "http://www.test.com/test.jpg", optionRequest)
        val response =
            RestAssured
                .given().log().all().body(product)
                .contentType(ContentType.JSON)
                .auth().oauth2(token)
                .`when`()
                .request("POST", "/api/admin/products")
                .then()
                .extract()
                .response()
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST.value())
        assertThat(
            response.body.jsonPath().get<String>("name"),
        ).contains("Invalid characters in name. Allowed: letters, numbers, spaces, (, ), [, ], +, -, &, /, _")
    }

    @Test
    fun `Should throw if price is 0`() {
        val optionRequest = listOf(OptionDto("option", 10))
        val product = ProductRequest("Table", 0.0, "http://www.test.com/test.jpg", optionRequest)
        val response =
            RestAssured
                .given().log().all().body(product)
                .contentType(ContentType.JSON)
                .auth().oauth2(token)
                .`when`()
                .request("POST", "/api/admin/products")
                .then()
                .extract()
                .response()
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST.value())
        assertThat(
            response.body.jsonPath().get<String>("price"),
        ).contains("Price must be greater than 0")
    }

    @Test
    fun `Should throw if price is lower`() {
        val optionRequest = listOf(OptionDto("option", 10))
        val product = ProductRequest("Table", -10.0, "http://www.test.com/test.jpg", optionRequest)
        val response =
            RestAssured
                .given().log().all().body(product)
                .contentType(ContentType.JSON)
                .auth().oauth2(token)
                .`when`()
                .request("POST", "/api/admin/products")
                .then()
                .extract()
                .response()
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST.value())
        assertThat(
            response.body.jsonPath().get<String>("price"),
        ).contains("Price must be greater than 0")
    }

    @Test
    fun `Should throw if url is not valid`() {
        val optionRequest = listOf(OptionDto("option", 10))
        val product = ProductRequest("Table", 10.0, "www.test.com", optionRequest)
        val response =
            RestAssured
                .given().log().all().body(product)
                .contentType(ContentType.JSON)
                .auth().oauth2(token)
                .`when`()
                .request("POST", "/api/admin/products")
                .then()
                .extract()
                .response()
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST.value())
        assertThat(
            response.body.jsonPath().get<String>("imageUrl"),
        ).contains("Invalid URL format, example: https://example.com/image.jpg")
    }
}
