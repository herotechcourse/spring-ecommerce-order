package ecommerce.controller.member

import ecommerce.controller.BaseApiTest
import ecommerce.dto.auth.LoginRequest
import ecommerce.dto.order.OrderResponse
import ecommerce.model.Cart
import ecommerce.model.CartProduct
import ecommerce.model.Option
import ecommerce.model.Product
import ecommerce.model.User
import ecommerce.repository.CartProductRepository
import ecommerce.repository.CartRepository
import ecommerce.repository.ProductRepository
import ecommerce.repository.UserRepository
import ecommerce.service.MemberAuthService
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
class MemberOrderControllerTest : BaseApiTest() {
    private lateinit var token: String

    @Autowired
    private lateinit var memberAuthService: MemberAuthService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var cartRepository: CartRepository

    @Autowired
    private lateinit var cartProductRepository: CartProductRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @BeforeEach
    fun beforeInit() {
        val member =
            userRepository.save(
                User(
                    "order@test.com",
                    "123456789",
                    "test",
                ),
            )
        val option =
            productRepository.save(
                Product(
                    "orderName",
                    "http://localhost:8080/image/upload/product1.jpg",
                    mutableListOf(
                        Option(
                            "name",
                            10.1,
                            51,
                            "http://localhost:8080/image/upload/product1.jpg",
                        ),
                    ),
                ),
            ).options.first()
        val cart = cartRepository.save(Cart(member))
        cart.items.add(cartProductRepository.save(CartProduct(option, 10)))
        cartRepository.save(cart)
        token = memberAuthService.login(LoginRequest(member.email, member.password))
    }

    @AfterEach
    fun afterInit() {
        cartProductRepository.deleteAll()
        cartRepository.deleteAll()
        productRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun getAllOrders() {
        val response =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().get("/api/member/order")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        assertThat(response.body().jsonPath().getList("orders", OrderResponse::class.java)).isNotNull
    }

    @Test
    fun `createCheckoutCartIntent & getOrderById`() {
        val intentResponse =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().post("/api/member/order/cart-checkout")
                .then().log().all().extract()

        val orderId = intentResponse.body().jsonPath().getLong("orderId")
        val response =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().get("/api/member/order/$orderId")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
    }

    @Test
    fun confirmCheckout() {
        val intentResponse =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().post("/api/member/order/cart-checkout")
                .then().log().all().extract()

        val orderId = intentResponse.body().jsonPath().getLong("orderId")
        val response =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().post("/api/member/order/confirm-checkout/$orderId")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        assertThat(response.body().jsonPath().getString("message")).isEqualTo("Order confirmed")
    }
}
