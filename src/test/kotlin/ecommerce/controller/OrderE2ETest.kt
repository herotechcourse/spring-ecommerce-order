package ecommerce.controller

import ecommerce.dto.OrderPlacementRequest
import ecommerce.dto.OrderResponseStatus
import ecommerce.dto.Role
import ecommerce.dto.TokenRequest
import ecommerce.model.Cart
import ecommerce.model.Member
import ecommerce.model.Option
import ecommerce.model.Product
import ecommerce.repository.CartItemRepository
import ecommerce.repository.CartRepository
import ecommerce.repository.MemberRepository
import ecommerce.repository.OptionRepository
import ecommerce.repository.OrderItemRepository
import ecommerce.repository.OrderRepository
import ecommerce.repository.ProductRepository
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderE2ETest() {
    @Autowired
    lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var cartRepository: CartRepository

    @Autowired
    private lateinit var optionRepository: OptionRepository

    @Autowired
    private lateinit var cartItemRepository: CartItemRepository

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var orderItemRepository: OrderItemRepository

    @LocalServerPort
    private var port: Int = 0

    val baseUrl get() = "http://localhost:$port"

    lateinit var optionS: Option
    lateinit var optionM: Option
    lateinit var product: Product
    lateinit var member: Member
    lateinit var cart: Cart
    lateinit var loginToken: String

    @BeforeEach
    fun setUp() {
        member =
            memberRepository.save(
                Member(
                    name = "user",
                    email = "user@mail.com",
                    password = "abcd1234",
                    role = Role.USER.name,
                ),
            )
        loginToken = loginAs(member.email, member.password)

        val coffee =
            Product(
                name = "coffee",
                price = 3.6,
                imageUrl = "https://coffee.com",
            )

        val s = Option(name = "S", quantity = 2)
        val m = Option(name = "M", quantity = 3)

        coffee.addOption(s) // sets option.product = coffee
        coffee.addOption(m)

        product = productRepository.save(coffee) // cascade saves options too
        optionS = product.options[0]
        optionM = product.options[1]
        cart = cartRepository.save(Cart(member = member))
        cart.addItem(product, 2)
        cartItemRepository.save(cart.items[0])
        cartRepository.save(cart)
    }

    @AfterEach
    fun tearDown() {
        cartItemRepository.deleteAll()
        cartRepository.deleteAll()
        orderRepository.deleteAll()
        orderItemRepository.deleteAll()
        memberRepository.deleteAll()
        productRepository.deleteAll()
        optionRepository.deleteAll()
    }

    private fun loginAs(
        email: String,
        password: String,
    ): String {
        val loginRequest = TokenRequest(email, password)
        val loginResponse =
            RestAssured
                .given()
                .baseUri(baseUrl)
                .body(loginRequest)
                .contentType(ContentType.JSON)
                .`when`()
                .post("/api/members/login")
                .then().log().all()
                .extract()
        val token = loginResponse.body().jsonPath().getString("token")
        return token
    }

    @ParameterizedTest
    @ValueSource(strings = ["pm_card_visa", "pm_card_amex", "pm_card_mastercard", "pm_card_discover"])
    fun `test valid payment methods`(method: String) {
        val req =
            OrderPlacementRequest(
                productOptionId = optionS.id,
                quantity = 1,
                paymentMethod = method,
            )

        val response =
            RestAssured
                .given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $loginToken")
                .body(req).contentType(ContentType.JSON)
                .`when`()
                .post("/api/orders/place")
                .then().log().all()
                .extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        assertThat(response.body().jsonPath().getString("status")).isEqualTo(OrderResponseStatus.SUCCESS.name)
        assertThat(response.body().jsonPath().getString("message")).isEqualTo("Payment successful. Order has been placed")
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "pm_card_visa_chargeDeclined",
            "pm_card_chargeDeclinedExpiredCard",
            "pm_card_chargeCustomerFail",
            "pm_card_visa_chargeDeclinedInsufficientFunds",
        ],
    )
    fun `test invalid payment methods`(method: String) {
        val req =
            OrderPlacementRequest(
                productOptionId = optionS.id,
                quantity = 1,
                paymentMethod = method,
            )

        val response =
            RestAssured
                .given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $loginToken")
                .body(req).contentType(ContentType.JSON)
                .`when`()
                .post("/api/orders/place")
                .then().log().all()
                .extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        assertThat(response.body().jsonPath().getString("status")).isEqualTo(OrderResponseStatus.FAILURE.name)
        assertThat(response.body().jsonPath().getString("message")).contains("Payment failed.")
    }

    private fun placeOrder(req: OrderPlacementRequest) {
        RestAssured
            .given()
            .baseUri(baseUrl)
            .header("Authorization", "Bearer $loginToken")
            .body(req).contentType(ContentType.JSON)
            .`when`()
            .post("/api/orders/place")
            .then().log().all()
            .extract()
    }

    @Test
    fun `test retrieving all orders for user`() {
        val req1 =
            OrderPlacementRequest(
                productOptionId = optionS.id,
                quantity = 1,
                paymentMethod = "pm_card_visa",
            )
        val req2 =
            OrderPlacementRequest(
                productOptionId = optionM.id,
                quantity = 2,
                paymentMethod = "pm_card_visa",
            )
        placeOrder(req1)
        placeOrder(req2)

        val response =
            RestAssured
                .given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $loginToken")
                .`when`()
                .get("/api/orders")
                .then().log().all()
                .extract()

        val jsonPath = response.body().jsonPath()

        val orders = jsonPath.getList<Any>("") // empty string to get root array
        assertThat(orders).hasSize(2)

        val paymentAmounts = jsonPath.getList<Int>("paymentAmount")
        assertThat(paymentAmounts).contains(360 * 2)
        assertThat(paymentAmounts).contains(360 * 1)

        val expected =
            listOf(
                listOf(mapOf("optionName" to "S", "productName" to "coffee", "quantity" to 1)),
                listOf(mapOf("optionName" to "M", "productName" to "coffee", "quantity" to 2)),
            )
        val actual: List<List<Map<String, Any>>> = jsonPath.getList("items")
        assertThat(actual).isEqualTo(expected)

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
    }

    @Test
    fun `test unavailable stock`() {
        val req =
            OrderPlacementRequest(
                productOptionId = optionS.id,
                quantity = 7,
                paymentMethod = "pm_card_visa",
            )

        val response =
            RestAssured
                .given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $loginToken")
                .body(req).contentType(ContentType.JSON)
                .`when`()
                .post("/api/orders/place")
                .then().log().all()
                .extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun `test empty cart after successful order of the option inside the cart`() {
        val req =
            OrderPlacementRequest(
                productOptionId = optionS.id,
                quantity = 1,
                paymentMethod = "pm_card_visa",
            )

        val itemsInCartBeforeRequest = cart.items[0].quantity

        val response =
            RestAssured
                .given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer $loginToken")
                .body(req).contentType(ContentType.JSON)
                .`when`()
                .post("/api/orders/place")
                .then().log().all()
                .extract()

        val updatedCart = cartRepository.findCartWithItemsByMemberId(member.id!!) ?: error("Cart not found")
        val itemsInCartAfterRequest = updatedCart.items[0].quantity

        assertThat(itemsInCartBeforeRequest).isEqualTo(2)
        assertThat(itemsInCartAfterRequest).isEqualTo(1)

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        assertThat(response.body().jsonPath().getString("status")).isEqualTo(OrderResponseStatus.SUCCESS.name)
        assertThat(response.body().jsonPath().getString("message")).isEqualTo("Payment successful. Order has been placed")
    }
}
