package ecommerce.controller.member

import ecommerce.controller.BaseApiTest
import ecommerce.dto.cartProduct.CartProductDto
import ecommerce.dto.user.UserRequestDTO
import ecommerce.model.Option
import ecommerce.model.Product
import ecommerce.repository.CartProductRepository
import ecommerce.repository.CartRepository
import ecommerce.repository.CartStatisticRepository
import ecommerce.repository.OptionRepository
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
class CartControllerTest : BaseApiTest() {
    @Autowired
    private lateinit var cartProductRepository: CartProductRepository

    @Autowired
    private lateinit var cartRepository: CartRepository
    lateinit var token: String
    lateinit var product: Product

    @Autowired
    lateinit var memberAuthService: MemberAuthService

    @Autowired
    lateinit var productRepository: ProductRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var optionRepository: OptionRepository

    @Autowired
    lateinit var cartStatisticRepository: CartStatisticRepository

    @BeforeEach
    fun beforeInit() {
        token = memberAuthService.signUp(UserRequestDTO("user", "user.test@test.com", "hello123")).token
        val options =
            mutableListOf(
                Option(
                    "hello-1",
                    10.0,
                    51,
                    "http://localhost:8080/image/upload/product1.jpg",
                ),
                Option(
                    "hello-2",
                    10.0,
                    51,
                    "http://localhost:8080/image/upload/product1.jpg",
                ),
            )
        product =
            productRepository.save(
                Product(
                    "test",
                    "http://localhost:8080/image/upload/product1.jpg",
                    options,
                ),
            )
    }

    @AfterEach
    fun afterInit() {
        cartStatisticRepository.deleteAll()
        cartProductRepository.deleteAll()
        cartRepository.deleteAll()
        optionRepository.deleteAll()
        productRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun getCartItems() {
        RestAssured
            .given().log().all()
            .header("Authorization", token)
            .contentType(ContentType.JSON)
            .`when`().post("/api/member/cart/${product.options.first().id}")
            .then().log().all().extract()
        val response =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().get("/api/member/cart")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        assertThat(response.body().jsonPath().getList("products", CartProductDto::class.java).size).isEqualTo(1)
    }

    @Test
    fun addProduct() {
        val response =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().post("/api/member/cart/${product.options.first().id}")
                .then().log().all().extract()

        val cartProductsResponse =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().get("/api/member/cart")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value())
        assertThat(cartProductsResponse.body().jsonPath().getList("products", CartProductDto::class.java).size).isEqualTo(1)
    }

    @Test
    fun `throws error for invalid product for addProduct`() {
        val response =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().post("/api/member/cart/-1")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value())
    }

    @Test
    fun removeProduct() {
        RestAssured
            .given().log().all()
            .header("Authorization", token)
            .contentType(ContentType.JSON)
            .`when`().post("/api/member/cart/${product.options.first().id}")
            .then().log().all().extract()

        val response =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().delete("/api/member/cart/${product.options.first().id}")
                .then().log().all().extract()

        val cartProductsResponse =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().get("/api/member/cart")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value())
        assertThat(cartProductsResponse.body().jsonPath().getList("products", CartProductDto::class.java).size).isZero
    }

    @Test
    fun clearCart() {
        RestAssured
            .given().log().all()
            .header("Authorization", token)
            .contentType(ContentType.JSON)
            .`when`().post("/api/member/cart/${product.options.first().id}")
            .then().log().all().extract()

        val response =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().delete("/api/member/cart/clear")
                .then().log().all().extract()

        val cartProductsResponse =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().get("/api/member/cart")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value())
        assertThat(cartProductsResponse.body().jsonPath().getList("products", CartProductDto::class.java).size).isZero
    }

    @Test
    fun `throws error for empty cart in clearCart`() {
        val response =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().delete("/api/member/cart/clear")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value())
    }
}
