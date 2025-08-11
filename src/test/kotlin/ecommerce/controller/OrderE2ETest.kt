package ecommerce.controller

import ecommerce.dto.OrderPlacementRequest
import ecommerce.dto.OrderResponseStatus
import ecommerce.dto.Role
import ecommerce.dto.TokenRequest
import ecommerce.model.Cart
import ecommerce.model.CartItem
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
        val cart = cartRepository.save(Cart(member = member))
        cartItemRepository.save(CartItem(coffee, cart, 2))
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

    @Test
    fun `test invalid payment methods`() {
    }

    @Test
    fun `test retrieving all orders for user`() {
    }

    @Test
    fun `test unavailable stock`() {
    }

    @Test
    fun `test empty cart after successful order of the option inside the cart`() {
    }
}
