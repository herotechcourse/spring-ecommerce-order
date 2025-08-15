package ecommerce.controller.admin

import ecommerce.controller.BaseApiTest
import ecommerce.dto.auth.LoginRequest
import ecommerce.dto.products.OptionDTO
import ecommerce.dto.products.OptionPatchDTO
import ecommerce.dto.products.ProductDTO
import ecommerce.dto.products.ProductPatchDTO
import ecommerce.enums.UserRole
import ecommerce.model.Option
import ecommerce.model.Product
import ecommerce.model.User
import ecommerce.repository.CartRepository
import ecommerce.repository.OptionRepository
import ecommerce.repository.ProductRepository
import ecommerce.repository.UserRepository
import ecommerce.service.AdminAuthService
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
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class AdminProductControllerTest : BaseApiTest() {
    private lateinit var token: String

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var adminAuthService: AdminAuthService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var optionRepository: OptionRepository

    @Autowired
    private lateinit var cartRepository: CartRepository

    @BeforeEach
    fun initBefore() {
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
        optionRepository.deleteAll()
        cartRepository.deleteAll()
        userRepository.deleteAll()
        productRepository.deleteAll()
    }

    @Test
    fun create() {
        val actual =
            ProductDTO(
                "test",
                "http://localhost:8080/image/upload/product1.jpg",
                mutableListOf(
                    OptionDTO(
                        "name",
                        10.1,
                        51,
                        "http://localhost:8080/image/upload/product1.jpg",
                    ),
                ),
            )
        val response =
            RestAssured
                .given().log().all()
                .body(actual)
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().post("/api/admin/products")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value())
    }

    @Test
    fun `throws error if validation fails create`() {
        val product =
            ProductDTO(
                "shouldFailTheTest",
                "http://localhost:8080/image/upload/product1.jpg",
                mutableListOf(
                    OptionDTO(
                        "name",
                        10.1,
                        51,
                        "http://localhost:8080/image/upload/product1.jpg",
                    ),
                ),
            )
        val response =
            RestAssured
                .given().log().all()
                .body(product)
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().post("/api/admin/products")
                .then().log().all().extract()
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
    }

    @ParameterizedTest
    @ValueSource(ints = [-1, 0])
    fun `throws error if validation fails create for option`(quantity: Int) {
        val actual =
            ProductDTO(
                "test",
                "http://localhost:8080/image/upload/product1.jpg",
                mutableListOf(
                    OptionDTO(
                        "name",
                        10.1,
                        quantity,
                        "http://localhost:8080/image/upload/product1.jpg",
                    ),
                ),
            )
        val response =
            RestAssured
                .given().log().all()
                .body(actual)
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().post("/api/admin/products")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun `Returns Products`() {
        val response =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .`when`().get("api/admin/products")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
    }

    @Test
    fun `Returns Product`() {
        val product = createProduct("Get One Product")
        val response =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .`when`().get("/api/admin/products/${product.id}")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
    }

    @Test
    fun update() {
        val product = createProduct("Update")
        val response =
            RestAssured
                .given().log().all()
                .body(
                    ProductDTO(
                        "Product2",
                        "http://localhost:8080/image/upload/product1.jpg",
                        mutableListOf(
                            OptionDTO(
                                "name",
                                10.1,
                                51,
                                "http://localhost:8080/image/upload/product1.jpg",
                            ),
                        ),
                    ),
                )
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().put("/api/admin/products/${product.id}")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
    }

    @Test
    fun patch() {
        val product = createProduct("Patch")
        val response =
            RestAssured
                .given().log().all()
                .body(
                    ProductPatchDTO("hello"),
                )
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().patch("/api/admin/products/${product.id}")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
    }

    @Test
    fun delete() {
        val product = createProduct("Delete")
        val response =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .`when`().delete("/api/admin/products/${product.id}")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value())
    }

    @Test
    fun options() {
        val product = createProduct("options")
        val response =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .`when`().get("/api/admin/products/${product.id}/options")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
    }

    @Test
    fun createOption() {
        val product = createProduct("createOption")
        val actual =
            OptionDTO(
                "test",
                10.1,
                51,
                "http://localhost:8080/image/upload/product1.jpg",
            )
        val response =
            RestAssured
                .given().log().all()
                .body(actual)
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().post("/api/admin/products/${product.id}/options")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value())
    }

    @Test
    fun updateOption() {
        val product = createProduct("createOption")
        val option = product.options[0]
        val actual =
            OptionDTO(
                "test",
                10.1,
                51,
                "http://localhost:8080/image/upload/product1.jpg",
            )
        val response =
            RestAssured
                .given().log().all()
                .body(actual)
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().put("/api/admin/products/${product.id}/options/${option.id}")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
    }

    @Test
    fun patchOption() {
        val product = createProduct("createOption")
        val option = product.options[0]
        val actual =
            OptionPatchDTO(
                "test",
                10.1,
                51,
                "http://localhost:8080/image/upload/product1.jpg",
            )
        val response =
            RestAssured
                .given().log().all()
                .body(actual)
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().patch("/api/admin/products/${product.id}/options/${option.id}")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
    }

    @Test
    fun deleteOption() {
        val product = createProduct("createOption")
        val option = product.options[0]

        val response =
            RestAssured
                .given().log().all()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .`when`().delete("/api/admin/products/${product.id}/options/${option.id}")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value())
    }

    private fun createProduct(name: String): Product {
        return productRepository.save(
            Product(
                name,
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
        )
    }
}
