package ecommerce.controller.member

import ecommerce.controller.BaseApiTest
import ecommerce.dto.auth.LoginRequest
import ecommerce.dto.user.UserRequestDTO
import ecommerce.model.User
import ecommerce.repository.UserRepository
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class MemberAuthControllerTest : BaseApiTest() {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `sign-up User`() {
        val user =
            UserRequestDTO(
                "test",
                "temp@temp.com",
                "test-456",
            )
        val response =
            RestAssured
                .given().log().all()
                .body(user)
                .contentType(ContentType.JSON)
                .`when`().post("api/member/auth/sign-up")
                .then().log().all().extract()
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value())
        assertThat(response.body().jsonPath().get<String>("token")).isNotEmpty
    }

    @Test
    fun login() {
        userRepository.save(
            User(
                "temp2@temp.com",
                "test-456",
                "test",
            ),
        )
        val loginRequest =
            LoginRequest(
                "temp2@temp.com",
                "test-456",
            )
        val response =
            RestAssured
                .given().log().all()
                .body(loginRequest)
                .contentType(ContentType.JSON)
                .`when`().post("api/member/auth/login")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        assertThat(response.body().jsonPath().get<String>("token")).isNotEmpty
    }
}
