package ecommerce.controller

import ecommerce.dto.AuthResponse
import ecommerce.dto.LoginForm
import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class CartStatsControllerTest() {
    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
    }

    @Test
    fun getTop5Products() {
        val email = "dan@htc.com"
        val password = "dan1234"
        val expected = 5

        val accessToken =
            RestAssured
                .given().log().all()
                .body(LoginForm(email, password))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .`when`().post("/api/members/login")
                .then().log().all().extract().`as`(AuthResponse::class.java).accessToken

        val response =
            RestAssured
                .given().log().all()
                .header("Authorization", "Bearer $accessToken")
                .`when`().get("/api/admin/cart-stats/top5-products")
                .then().log().all()
                .assertThat().statusCode(HttpStatus.OK.value())
                .extract()

        val actual = response.jsonPath().getList<Map<String, Any>>("")
        assertThat(actual.size).isEqualTo(expected)
    }

    @Test
    fun `getTop5Products() - return 403 when not admin member`() {
        val email = "ann@htc.com"
        val password = "ann1234"

        val accessToken =
            RestAssured
                .given().log().all()
                .body(LoginForm(email, password))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .`when`().post("/api/members/login")
                .then().log().all().extract().`as`(AuthResponse::class.java).accessToken

        RestAssured
            .given().log().all()
            .header("Authorization", "Bearer $accessToken")
            .`when`().get("/api/admin/cart-stats/top5-products")
            .then().log().all()
            .assertThat().statusCode(HttpStatus.FORBIDDEN.value())
    }

    @Test
    fun `getTop5Products() - return 401 when not authenticated`() {
        val accessToken = "abc"

        RestAssured
            .given().log().all()
            .header("Authorization", "Bearer $accessToken")
            .`when`().get("/api/admin/cart-stats/top5-products")
            .then().log().all()
            .assertThat().statusCode(HttpStatus.UNAUTHORIZED.value())
    }

    @Test
    fun getActiveMembers() {
        val email = "dan@htc.com"
        val password = "dan1234"

        val accessToken =
            RestAssured
                .given().log().all()
                .body(LoginForm(email, password))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .`when`().post("/api/members/login")
                .then().log().all().extract().`as`(AuthResponse::class.java).accessToken

        val response =
            RestAssured
                .given().log().all()
                .header("Authorization", "Bearer $accessToken")
                .`when`().get("/api/admin/cart-stats/active-members")
                .then().log().all()
                .assertThat().statusCode(HttpStatus.OK.value())
                .extract()

        val actual = response.jsonPath().getList<Map<String, Any>>("")
        assertThat(actual).isNotNull()
        assertThat(actual.size).isNotNull()
        assertThat(actual.size).isNotZero()
    }

    @Test
    fun `getActiveMembers() - return 403 when not admin member`() {
        val email = "ann@htc.com"
        val password = "ann1234"

        val accessToken =
            RestAssured
                .given().log().all()
                .body(LoginForm(email, password))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .`when`().post("/api/members/login")
                .then().log().all().extract().`as`(AuthResponse::class.java).accessToken

        RestAssured
            .given().log().all()
            .header("Authorization", "Bearer $accessToken")
            .`when`().get("/api/admin/cart-stats/active-members")
            .then().log().all()
            .assertThat().statusCode(HttpStatus.FORBIDDEN.value())
    }

    @Test
    fun `getActiveMembers() - return 401 when not authenticated`() {
        val accessToken = "abc"

        RestAssured
            .given().log().all()
            .header("Authorization", "Bearer $accessToken")
            .`when`().get("/api/admin/cart-stats/active-members")
            .then().log().all()
            .assertThat().statusCode(HttpStatus.UNAUTHORIZED.value())
    }
}
