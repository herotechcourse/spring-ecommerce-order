package ecommerce.controller

import ecommerce.controller.api.AuthController
import ecommerce.dto.auth.AuthResponse
import ecommerce.dto.member.LoginForm
import ecommerce.dto.member.MemberResponse
import ecommerce.dto.member.RegisterForm
import ecommerce.model.Member
import ecommerce.repository.MemberRepository
import ecommerce.service.AuthService
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.controller

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class AuthControllerTest(
    @Autowired private val memberRepository: MemberRepository,
    @Autowired private val controller: AuthController,
) {
    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
    }

    @AfterEach
    fun cleanUp() {
        memberRepository.deleteAll()
    }

    @Test
    fun registerMember() {
        val email = "test@test.com"
        val password = "test1234"
        val testForm = RegisterForm(email, password)
        val response = controller.registerMember(testForm)
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body).isInstanceOf(AuthResponse::class.java)
    }

    @Test
    fun `registerMember() - should insert member and return 201 when form is valid`() {
        val response =
            RestAssured
                .given().log().all()
                .body(Member(email = "test@email.com", password = "test1234"))
                .contentType(ContentType.JSON)
                .`when`().post("/api/members/register")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value())
        assertThat(response.body().jsonPath().getString("accessToken")).isInstanceOf(String::class.java)
    }

    @Test
    fun `registerMember() - should return 400 when email is blank`() {
        val expected = "Email is required"
        RestAssured
            .given().log().all()
            .body(Member(email = "", password = "test1234"))
            .contentType(ContentType.JSON)
            .`when`().post("/api/members/register")
            .then().log().all()
            .assertThat()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("errors[0].message", equalTo(expected))
    }

    @Test
    fun `registerMember() - should return 400 when email is not valid`() {
        val expected = "Email must be valid"
        RestAssured
            .given().log().all()
            .body(Member(email = "abc.abc.com", password = "test1234"))
            .contentType(ContentType.JSON)
            .`when`().post("/api/members/register")
            .then().log().all()
            .assertThat()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("errors[0].message", equalTo(expected))
    }

    @Test
    fun `registerMember() - should return 400 when password is shorter than 4 characters long`() {
        val expected = "Password must be at least 4 characters long"
        RestAssured
            .given().log().all()
            .body(Member(email = "test@email.com", password = "123"))
            .contentType(ContentType.JSON)
            .`when`().post("/api/members/register")
            .then().log().all()
            .assertThat()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("errors[0].message", equalTo(expected))
    }

    @Test
    fun `registerMember() - should return 400 when email already exists`() {
        val targetEmail = "dan@htc.com"
        val expected = AuthService.MESSAGE_EMAIL_ALREADY_EXISTS
        RestAssured
            .given().log().all()
            .body(Member(email = targetEmail, password = "test1234"))
            .contentType(ContentType.JSON)
            .`when`().post("/api/members/register")
            .then().log().all()

        RestAssured
            .given().log().all()
            .body(Member(email = targetEmail, password = "test1234"))
            .contentType(ContentType.JSON)
            .`when`().post("/api/members/register")
            .then().log().all()
            .assertThat()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("errors.find { it.field == 'name' }.message", equalTo(expected))
    }

    @Test
    fun loginMember() {
        val email = "san@htc.com"
        val password = "san1234"
        val testForm = LoginForm(email, password)
        val response = controller.loginMember(testForm)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isInstanceOf(AuthResponse::class.java)
    }

    @Test
    fun `loginMember() - should login member and return 200 when form is valid`() {
        val targetEmail = "dan@htc.com"

        val response =
            RestAssured
                .given().log().all()
                .body(Member(email = targetEmail, password = "dan1234"))
                .contentType(ContentType.JSON)
                .`when`().post("/api/members/login")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        assertThat(response.body().jsonPath().getString("accessToken")).isInstanceOf(String::class.java)
    }

    @Test
    fun `loginMember() - should return 400 when email is blank`() {
        val expected = "Please enter your email address"
        RestAssured
            .given().log().all()
            .body(Member(email = "", password = "test1234"))
            .contentType(ContentType.JSON)
            .`when`().post("/api/members/login")
            .then().log().all()
            .assertThat()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("errors[0].message", equalTo(expected))
    }

    @Test
    fun `loginMember() - should return 400 when password is blank`() {
        val expected = "Please enter your password"
        RestAssured
            .given().log().all()
            .body(Member(email = "test@email.com", password = ""))
            .contentType(ContentType.JSON)
            .`when`().post("/api/members/login")
            .then().log().all()
            .assertThat()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("errors[0].field", equalTo("password"))
            .body("errors[0].message", equalTo(expected))
    }

    @Test
    fun `loginMember() - should return 401 when email is invalid`() {
        val targetEmail = "dan@htc.com"
        RestAssured
            .given().log().all()
            .body(Member(email = targetEmail, password = "test1234"))
            .contentType(ContentType.JSON)
            .`when`().post("/api/members/register")
            .then().log().all()

        RestAssured
            .given().log().all()
            .body(Member(email = "test123@email.com", password = "test1234"))
            .contentType(ContentType.JSON)
            .`when`().post("/api/members/login")
            .then().log().all()
            .assertThat()
            .statusCode(HttpStatus.UNAUTHORIZED.value())
    }

    @Test
    fun `loginMember() - should return 401 when password is invalid`() {
        val targetEmail = "dan@htc.com"
        RestAssured
            .given().log().all()
            .body(Member(email = targetEmail, password = "test1234"))
            .contentType(ContentType.JSON)
            .`when`().post("/api/members/register")
            .then().log().all()

        RestAssured
            .given().log().all()
            .body(Member(email = targetEmail, password = "invalid1234"))
            .contentType(ContentType.JSON)
            .`when`().post("/api/members/login")
            .then().log().all()
            .assertThat()
            .statusCode(HttpStatus.UNAUTHORIZED.value())
    }

    @Test
    fun tokenLogin() {
        val targetEmail = "jason@htc.com"
        RestAssured
            .given().log().all()
            .body(Member(email = targetEmail, password = "jason1234"))
            .contentType(ContentType.JSON)
            .`when`().post("/api/members/register")
            .then().log().all()

        val accessToken =
            RestAssured
                .given().log().all()
                .body(LoginForm(targetEmail, "jason1234"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .`when`().post("/api/members/login")
                .then().log().all().extract().`as`(AuthResponse::class.java).accessToken

        val member =
            RestAssured
                .given().log().all()
                .header("Authorization", "Bearer $accessToken")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .`when`().get("/api/members/me")
                .then().log().all()
                .statusCode(HttpStatus.OK.value()).extract().`as`(MemberResponse::class.java)

        assertThat(member.email).isEqualTo(targetEmail)
    }
}
