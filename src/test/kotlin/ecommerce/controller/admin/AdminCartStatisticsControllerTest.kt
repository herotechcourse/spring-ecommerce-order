package ecommerce.controller.admin

import ecommerce.controller.BaseApiTest
import ecommerce.dto.auth.LoginRequest
import ecommerce.enums.UserRole
import ecommerce.model.User
import ecommerce.repository.UserRepository
import ecommerce.service.AdminAuthService
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class AdminCartStatisticsControllerTest : BaseApiTest() {
    private lateinit var token: String

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var adminAuthService: AdminAuthService

    @BeforeEach
    fun initBefore() {
        RestAssured.port = 80
        val user =
            userRepository.save(
                User(
                    "admin@testing.com",
                    "testPassword",
                    "testUser",
                    UserRole.ADMIN,
                ),
            )
        token = adminAuthService.login(LoginRequest(user.email, user.password))
    }

    @AfterEach
    fun initAfter() {
        val user = userRepository.findByEmail("admin@testing.com").orElseThrow()
        userRepository.delete(user)
    }

    @Test
    fun adminStatisticsService() {
        val response =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().get("/api/admin/cart-statistics/top-products")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
    }

    @Test
    fun getMembersWhoAddedToCart() {
        val response =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().get("/api/admin/cart-statistics/members-added-cart")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
    }
}
