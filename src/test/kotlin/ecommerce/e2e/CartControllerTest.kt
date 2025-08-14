package ecommerce.e2e

import ecommerce.dto.CartItemRequest
import ecommerce.dto.RegistrationRequest
import ecommerce.enum.CartHistoryStatus
import ecommerce.repository.CartHistoryJpaRepository
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
class CartControllerTest {
    lateinit var token: String

    @Autowired
    private lateinit var cartHistoryJpaRepository: CartHistoryJpaRepository

    @BeforeEach
    fun setUp() {
        val registrationRequest =
            RegistrationRequest(
                "test",
                "test1@test.com",
                "12345678",
            )

        token =
            RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(registrationRequest)
                .post("/api/members/register")
                .then().extract().body().jsonPath().getString("token")
    }

    @Test
    fun addToCart() {
        val productToCart =
            CartItemRequest(
                optionId = 1,
                quantity = 2,
            )

        val addProduct =
            RestAssured.given().log().all()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(productToCart)
                .`when`().post("/api/user/wishes")
                .then().log().all().extract()

        Assertions.assertThat(addProduct.statusCode()).isEqualTo(HttpStatus.OK.value())
        val json = JSONObject(addProduct.asString())
        assertThat(json.get("productName")).isEqualTo("Espresso")
        assertThat(json.get("quantity")).isEqualTo(2)
    }

    @Test
    fun `update quantity if product already in cart`() {
        val productToCart =
            CartItemRequest(
                optionId = 1,
                quantity = 2,
            )

        RestAssured.given().log().all()
            .auth().oauth2(token)
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(productToCart)
            .`when`().post("/api/user/wishes")
            .then().log().all().extract()

        val newProduct =
            CartItemRequest(
                optionId = 1,
                quantity = 4,
            )

        val response =
            RestAssured.given().log().all()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(newProduct)
                .`when`().post("/api/user/wishes")
                .then().log().all().extract()

        Assertions.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        val json = JSONObject(response.asString())
        assertThat(json.get("quantity")).isEqualTo(4)
    }

    @Test
    fun getCartItems() {
        addToCart()

        val response =
            RestAssured.given().log().all()
                .auth().oauth2(token)
                .accept(ContentType.JSON)
                .`when`().get("/api/user/wishes")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        val jsonObject = JSONObject(response.asString())
        assertThat(jsonObject.get("totalElements")).isEqualTo(1)
        val productName = response.body().jsonPath().getString("content[0].productName")
        assertThat(productName).isEqualTo("Espresso")
    }

    @Test
    fun `should remove item from cart if exists`() {
        addToCart()

        val getResponse =
            RestAssured.given().log().all()
                .auth().oauth2(token)
                .accept(ContentType.JSON)
                .`when`().get("/api/user/wishes")
                .then().log().all().extract()

        assertThat(getResponse.statusCode()).isEqualTo(HttpStatus.OK.value())
        val jsonObjectBefore = JSONObject(getResponse.asString())
        assertThat(jsonObjectBefore.get("totalElements")).isEqualTo(1)

        val deleteResponse =
            RestAssured.given().log().all()
                .auth().oauth2(token)
                .accept(ContentType.JSON)
                .`when`().delete("/api/user/wishes/1")
                .then().log().all().extract()

        assertThat(deleteResponse.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value())

        val getAfterDeleteResponse =
            RestAssured.given().log().all()
                .auth().oauth2(token)
                .accept(ContentType.JSON)
                .`when`().get("/api/user/wishes")
                .then().log().all().extract()

        assertThat(getAfterDeleteResponse.statusCode()).isEqualTo(HttpStatus.OK.value())
        val jsonObjectAfter = JSONObject(getAfterDeleteResponse.asString())
        assertThat(jsonObjectAfter.get("totalElements")).isEqualTo(0)
    }

    @Test
    fun `when product added to cart should add a new cart history element in the table`() {
        val productToCart =
            CartItemRequest(
                optionId = 1,
                quantity = 2,
            )

        val addProduct =
            RestAssured.given().log().all()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(productToCart)
                .`when`().post("/api/user/wishes")
                .then().log().all().extract()

        Assertions.assertThat(addProduct.statusCode()).isEqualTo(HttpStatus.OK.value())
        val historyEntries = cartHistoryJpaRepository.findAll()
        Assertions.assertThat(historyEntries).anySatisfy {
            Assertions.assertThat(it.option.id).isEqualTo(productToCart.optionId)
            Assertions.assertThat(it.quantity).isEqualTo(productToCart.quantity)
            Assertions.assertThat(it.status).isEqualTo(CartHistoryStatus.ADDED)
        }
    }
}
