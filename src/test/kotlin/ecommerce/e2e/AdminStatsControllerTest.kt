package ecommerce.e2e

import ecommerce.dto.LoginRequest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminStatsControllerTest {
    lateinit var token: String

    @BeforeEach
    fun setUp() {
        val loginRequest =
            LoginRequest(
                "admin@test.com",
                "12345678",
            )

        val loginResponse =
            RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .post("/api/members/login")
                .then().extract()

        token = loginResponse.body().jsonPath().getString("token")
    }

    @Test
    fun getTopProducts() {
        val response =
            RestAssured.given().log().all()
                .auth().oauth2(token)
                .accept(ContentType.JSON)
                .`when`().get("/api/admin/stats/top-products")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        val products = response.body().jsonPath().getList<String>("productName")
        assertThat(products).isNotEmpty()
        assertThat(products.size).isEqualTo(3)
        assertThat(products).contains("Reusable Coffee Cup")
        assertThat(products).contains("Cappuccino")
    }

    @Test
    fun getTopActiveUsers() {
        val response =
            RestAssured.given().log().all()
                .auth().oauth2(token)
                .accept(ContentType.JSON)
                .`when`().get("/api/admin/stats/active-users")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        val users = response.body().jsonPath()
        assertThat(users.getList<Int>("memberId")).hasSize(2)
        assertThat(users.getList<String>("memberEmail")).contains("user1@example.com")
        assertThat(users.getList<String>("memberName")).contains("user1")
    }
}
