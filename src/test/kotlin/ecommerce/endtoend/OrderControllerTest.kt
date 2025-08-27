package ecommerce.endtoend

import ecommerce.dto.LoginRequest
import ecommerce.dto.stripe.PaymentRequest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class OrderControllerTest {
    lateinit var token: String

    @BeforeEach
    fun setUp() {
        val loginRequest =
            LoginRequest(
                "user1@example.com",
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

    @Test
    fun placeSuccessfulOrder() {
        val response =
            RestAssured.given().log().all()
                .auth().oauth2(token)
                .accept(ContentType.JSON)
                .`when`().get("/api/user/wishes")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        val jsonObject = JSONObject(response.asString())
        assertThat(jsonObject.get("totalElements")).isEqualTo(2)

        val paymentRequest = PaymentRequest("EUR", "pm_card_visa")
        val orderResponse =
            RestAssured.given().log().all()
                .auth().oauth2(token)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(paymentRequest)
                .post("/api/orders/place")
                .then().log().all().extract()

        assertThat(orderResponse.statusCode()).isEqualTo(HttpStatus.OK.value())

        val newResponse =
            RestAssured.given().log().all()
                .auth().oauth2(token)
                .accept(ContentType.JSON)
                .`when`().get("/api/user/wishes")
                .then().log().all().extract()

        val jsonObject2 = JSONObject(newResponse.asString())
        assertThat(jsonObject2.get("totalElements")).isEqualTo(0)
    }

    @Test
    fun placeUnsuccessfulOrder() {
        val response =
            RestAssured.given().log().all()
                .auth().oauth2(token)
                .accept(ContentType.JSON)
                .`when`().get("/api/user/wishes")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        val jsonObject = JSONObject(response.asString())
        assertThat(jsonObject.get("totalElements")).isEqualTo(2)

        val paymentRequest = PaymentRequest("EUR", "pm_card_visa_chargeDeclinedInsufficientFunds")
        val orderResponse =
            RestAssured.given().log().all()
                .auth().oauth2(token)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(paymentRequest)
                .post("/api/orders/place")
                .then().log().all().extract()

        assertThat(orderResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())

        val newResponse =
            RestAssured.given().log().all()
                .auth().oauth2(token)
                .accept(ContentType.JSON)
                .`when`().get("/api/user/wishes")
                .then().log().all().extract()

        val jsonObject2 = JSONObject(newResponse.asString())
        assertThat(jsonObject2.get("totalElements")).isEqualTo(2)
    }
}
