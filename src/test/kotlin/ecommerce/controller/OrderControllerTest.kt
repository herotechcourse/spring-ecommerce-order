package ecommerce.controller

import ecommerce.controller.api.OrderController
import ecommerce.dto.AuthResponse
import ecommerce.dto.LoginForm
import ecommerce.dto.OrderPlaceForm
import ecommerce.model.Order
import ecommerce.repository.MemberRepository
import ecommerce.service.OrderService
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class OrderControllerTest(
    @Autowired private val controller: OrderController,
    @Autowired private val orderService: OrderService,
) {
    @Autowired
    private lateinit var memberRepository: MemberRepository

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
    }

    @Test
    fun placeOrder() {
        val member = memberRepository.findById(1L).get()
        val cartItemId = 2L
        val orderForm = OrderPlaceForm(listOf(cartItemId))
        val response = controller.placeOrder(orderForm, member)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotEmpty()
        assertThat(response.body).hasSize(1)
    }

    @Test
    fun `placeOrder() - return order with 200 OK when order processed well`() {
        val email = "san@htc.com"
        val password = "san1234"
        val cartItemId = 2L
        val orderForm = OrderPlaceForm(listOf(cartItemId))

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
            .body(orderForm)
            .contentType(ContentType.JSON)
            .`when`().post("/api/orders/place")
            .then().log().all()
            .assertThat().statusCode(HttpStatus.OK.value())
    }

    @Test
    fun `placeOrder() - return 401 Unauthorized when credential invalid`() {
        val email = "san@htc.com"
        val password = "san1234"
        val cartItemId = 2L
        val orderForm = OrderPlaceForm(listOf(cartItemId))

        val accessToken =
            RestAssured
                .given().log().all()
                .body(LoginForm(email, password))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .`when`().post("/api/members/login")
                .then().log().all().extract().`as`(AuthResponse::class.java).accessToken

        val contaminatedToken = accessToken + 123
        RestAssured
            .given().log().all()
            .header("Authorization", "Bearer $contaminatedToken")
            .body(orderForm)
            .contentType(ContentType.JSON)
            .`when`().post("/api/orders/place")
            .then().log().all()
            .assertThat().statusCode(HttpStatus.UNAUTHORIZED.value())
    }

    @Test
    fun `placeOrder() - return 400 Bad request when cart is empty`() {
        val email = "san@htc.com"
        val password = "san1234"
        val orderForm = OrderPlaceForm(listOf())

        val accessToken =
            RestAssured
                .given().log().all()
                .body(LoginForm(email, password))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .`when`().post("/api/members/login")
                .then().log().all().extract().`as`(AuthResponse::class.java).accessToken

        val contaminatedToken = accessToken + 123
        RestAssured
            .given().log().all()
            .header("Authorization", "Bearer $contaminatedToken")
            .body(orderForm)
            .contentType(ContentType.JSON)
            .`when`().post("/api/orders/place")
            .then().log().all()
            .assertThat().statusCode(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun `placeOrder() - return 400 Bad request when stock is insufficient`() {
        val email = "san@htc.com"
        val password = "san1234"
        val cartItemId = 15L
        val orderForm = OrderPlaceForm(listOf(cartItemId))

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
            .body(orderForm)
            .contentType(ContentType.JSON)
            .`when`().post("/api/orders/place")
            .then().log().all()
            .assertThat().statusCode(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun `placeOrder() - return 402 Payment required when payment method invalid`() {
        val email = "san@htc.com"
        val password = "san1234"
        val cartItemId = 2L
        val orderForm = OrderPlaceForm(listOf(cartItemId), "pm_card_visa_chargeDeclined")

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
            .body(orderForm)
            .contentType(ContentType.JSON)
            .`when`().post("/api/orders/place")
            .then().log().all()
            .assertThat().statusCode(HttpStatus.PAYMENT_REQUIRED.value())
    }

    @Test
    fun getOrdersByMemberId() {
        placeOrder()
        val member = memberRepository.findAll().first()
        val orders = controller.getOrdersByMemberId(member)

        assertThat(orders).isNotNull()
        assertThat(orders.body).isNotEmpty()
    }

    @Test
    fun getOrderById() {
        val member = memberRepository.findAll().first()
        val cartItemId = 12L
        val savedOrder: Order = orderService.placeOrder(member.id, OrderPlaceForm(listOf(cartItemId)))
        val order = controller.getOrderById(savedOrder.id, member)

        assertThat(order).isNotNull()
        assertThat(order.body?.orderStatus).isEqualTo(savedOrder.status)
        assertThat(order.body?.orderDate).isEqualTo(savedOrder.createdAt)
    }
}
