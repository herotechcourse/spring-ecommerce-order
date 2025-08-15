package ecommerce.controller.guest

import ecommerce.controller.BaseApiTest
import ecommerce.dto.products.ProductResponseDTO
import ecommerce.model.Option
import ecommerce.model.Product
import ecommerce.repository.OptionRepository
import ecommerce.repository.ProductRepository
import io.restassured.RestAssured
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
class GuestProductControllerTest : BaseApiTest() {
    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var optionRepository: OptionRepository

    @BeforeEach
    fun initBefore() {
        val allProducts =
            (0..20).map {
                Product(
                    "name-$it",
                    "http://localhost:8080/image/upload/product1.jpg",
                    mutableListOf(
                        Option(
                            "option-$it",
                            10.0,
                            51,
                            "http://localhost:8080/image/upload/product1.jpg",
                        ),
                    ),
                )
            }
        productRepository.saveAll(allProducts)
    }

    @AfterEach
    fun initAfter() {
        optionRepository.deleteAll()
        productRepository.deleteAll()
    }

    @Test
    fun `check default response listProducts`() {
        val response =
            RestAssured
                .given().log().all()
                .`when`().get("api/products")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        assertThat(response.body().jsonPath().get<List<ProductResponseDTO>>("content")).hasSize(10)
    }

    @Test
    fun `check paginated listProducts`() {
        val response =
            RestAssured
                .given().log().all()
                .`when`().get("api/products?page=1&perPage=5")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
        assertThat(response.body().jsonPath().get<List<ProductResponseDTO>>("content")).hasSize(5)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, -5])
    fun `throws error on negative page`(page: Int) {
        val response =
            RestAssured
                .given().log().all()
                .`when`().get("api/products?page=$page&perPage=5")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
    }

    @ParameterizedTest
    @ValueSource(ints = [0, -5])
    fun `throws error on negative perPage`(perPage: Int) {
        val response =
            RestAssured
                .given().log().all()
                .`when`().get("api/products?page=1&perPage=$perPage")
                .then().log().all().extract()

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
    }
}
