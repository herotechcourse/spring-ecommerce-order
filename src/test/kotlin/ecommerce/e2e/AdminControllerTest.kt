package ecommerce.e2e

import ecommerce.dto.CartItemRequest
import ecommerce.dto.LoginRequest
import ecommerce.dto.OptionDto
import ecommerce.dto.ProductRequest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
class AdminControllerTest {
    lateinit var token: String
    lateinit var auth: RequestSpecification

    @BeforeEach
    fun setUp() {
        val loginRequest =
            LoginRequest(
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
        auth =
            RestAssured.given().log().all()
                .auth().oauth2(token)
                .accept(ContentType.JSON)
    }

    @Test
    fun getAllProducts() {
        val response =
            auth
                .`when`().get("/api/admin/products")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        val jsonObject = JSONObject(response.asString())
        assertThat(jsonObject.get("totalPages")).isEqualTo(2)
        assertThat(jsonObject.get("totalElements")).isEqualTo(10)
    }

    @Test
    fun getProductById() {
        val response =
            auth
                .`when`().get("/api/admin/products/1")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        val productName = response.body().jsonPath().getString("name")
        assertThat(productName).isEqualTo("Espresso")
    }

    @Test
    fun createProduct() {
        val productRequest =
            ProductRequest(
                "newProductTest",
                2.99,
                "http://www.newProduct.jpg",
                listOf(OptionDto("option1", 10), OptionDto("option2", 10)),
            )

        val response =
            auth
                .contentType(ContentType.JSON)
                .body(productRequest)
                .post("/api/admin/products")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())

        val productName = response.body().jsonPath().getString("name")
        assertThat(productName).isEqualTo("newProductTest")
        val json = JSONObject(response.asString())
        assertThat(json.getJSONArray("options").length()).isEqualTo(2)
    }

    @Test
    fun updateProduct() {
        val productRequest =
            ProductRequest(
                "updatedTest",
                2.99,
                "http://www.newProduct.jpg",
                listOf(OptionDto("option1", 10)),
            )

        val response =
            auth
                .contentType(ContentType.JSON)
                .body(productRequest)
                .patch("/api/admin/products/1")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())

        val productName = response.body().jsonPath().getString("name")
        assertThat(productName).isEqualTo("updatedTest")
    }

    @Test
    fun deleteProduct() {
        val response =
            auth
                .contentType(ContentType.JSON)
                .delete("/api/admin/products/1")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value())

        val deleted =
            RestAssured.given().log().all()
                .auth().oauth2(token)
                .accept(ContentType.JSON)
                .`when`().get("/api/admin/products/1")
                .then().log().all().extract()

        assertThat(deleted.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value())
    }

    @Test
    fun addOption() {
        val optionRequest = OptionDto("newOptionTest", 10)

        val response =
            auth
                .contentType(ContentType.JSON)
                .body(optionRequest)
                .post("/api/admin/products/add/option/1")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())

        val json = JSONObject(response.asString())
        val array = json.getJSONArray("options")
        assertThat(array.length()).isEqualTo(2)
        assertThat(array.getJSONObject(1).getString("name")).isEqualTo("newOptionTest")
    }

    @Test
    fun `delete product even if is in a cart and in the cart history`() {
        val addItemToCart =
            auth
                .contentType(ContentType.JSON)
                .body(CartItemRequest(1, 5))
                .post("/api/user/wishes")
                .then().log().all().extract()

        val response =
            auth
                .contentType(ContentType.JSON)
                .delete("/api/admin/products/1")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value())

        val deleted =
            auth
                .`when`().get("/api/admin/products/1")
                .then().log().all().extract()

        assertThat(deleted.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value())
    }

    @Test
    fun `should throw and respond with 404 if delete a not existing product`() {
        val response =
            auth
                .contentType(ContentType.JSON)
                .delete("/api/admin/products/100")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value())
    }
}
