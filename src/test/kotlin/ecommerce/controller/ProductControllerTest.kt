package ecommerce.controller

import ecommerce.controller.api.ProductController
import ecommerce.dto.product.ProductForm
import ecommerce.exception.NotFoundException
import ecommerce.repository.ProductRepository
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional
import kotlin.collections.listOf

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class ProductControllerTest(
    @Autowired private val productRepository: ProductRepository,
    @Autowired private val controller: ProductController,
) {
    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        RestAssured.port = port
    }

    @Test
    fun `create() - should insert product and return 201 when form is valid`() {
        RestAssured
            .given().log().all()
            .body(ProductForm(name = "product1New", price = 1.5, imageUrl = "https://www.product.com/image/1"))
            .contentType(ContentType.JSON)
            .`when`().post("/api/products")
            .then().log().all()
            .assertThat()
            .statusCode(HttpStatus.CREATED.value())
    }

    @Test
    fun `create() - should return 400 when name is blank`() {
        val response =
            RestAssured
                .given().log().all()
                .body(ProductForm(name = "", price = 1.5, imageUrl = "https://www.product.com/image/1"))
                .contentType(ContentType.JSON)
                .`when`().post("/api/products")
                .then().log().all().extract()

        val targets =
            listOf(
                "Contains unallowed character",
                "Product name is required",
                "Must be no more than 15 characters, including spaces",
            )

        val actualErrorMessage = response.jsonPath().getString("errors[0].message")

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        assertThat(actualErrorMessage).isIn(targets)
    }

    @Test
    fun `create() - should return 400 when name is more than 15 characters`() {
        val expected = "Must be no more than 15 characters, including spaces"
        val response =
            RestAssured
                .given().log().all()
                .body(
                    ProductForm(
                        name = "this is very long name",
                        price = 1.5,
                        imageUrl = "https://www.product.com/image/1",
                    ),
                )
                .contentType(ContentType.JSON)
                .`when`().post("/api/products")
                .then().log().all()
                .extract()

        val actualErrorMessage = response.jsonPath().getString("errors[0].message")

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        assertThat(actualErrorMessage).isIn(expected)
    }

    @Test
    fun `create() - should return 400 when name has unallowed character`() {
        val expected = "Contains unallowed character"
        val response =
            RestAssured
                .given().log().all()
                .body(ProductForm(name = "I am product!", price = 1.5, imageUrl = "https://www.product.com/image/1"))
                .contentType(ContentType.JSON)
                .`when`().post("/api/products")
                .then().log().all()
                .extract()

        val actualErrorMessage = response.jsonPath().getString("errors[0].message")

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        assertThat(actualErrorMessage).isIn(expected)
    }

    @Test
    fun `create() - should return 400 when price is 0`() {
        val expected = "Product price must be greater than zero"
        val response =
            RestAssured
                .given().log().all()
                .body(ProductForm(name = "base", price = 0.0, imageUrl = "https://www.product.com/image/1"))
                .contentType(ContentType.JSON)
                .`when`().post("/api/products")
                .then().log().all()
                .extract()

        val actualErrorMessage = response.jsonPath().getString("errors[0].message")

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        assertThat(actualErrorMessage).isIn(expected)
    }

    @Test
    fun `create() - should return 400 when image URL is blank`() {
        val response =
            RestAssured
                .given().log().all()
                .body(ProductForm(name = "base", price = 2.0, imageUrl = ""))
                .contentType(ContentType.JSON)
                .`when`().post("/api/products")
                .then().log().all().extract()

        val expected =
            listOf(
                "Product image URL is required",
                "Must start with 'https://'.",
            )
        val actualErrorMessage = response.jsonPath().getString("errors[0].message")

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        assertThat(actualErrorMessage).isIn(expected)
    }

    @Test
    fun `create() - should return 400 when image URL is not valid`() {
        val expected = "Must start with 'https://'."
        val response =
            RestAssured
                .given().log().all()
                .body(ProductForm(name = "base", price = 2.0, imageUrl = "ssh://www.product.com/image/1"))
                .contentType(ContentType.JSON)
                .`when`().post("/api/products")
                .then().log().all()
                .extract()

        val actualErrorMessage = response.jsonPath().getString("errors[0].message")

        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        assertThat(actualErrorMessage).isIn(expected)
    }

    @Test
    fun `create() - should return 400 when name of product already exists`() {
        val name = "Iron Man"
        val expected = "Product with name '$name' already exists."

        RestAssured
            .given().log().all()
            .body(ProductForm(name = name, price = 2.0, imageUrl = "https://www.product.com/image/1"))
            .contentType(ContentType.JSON)
            .`when`().post("/api/products")
            .then().log().all()
            .assertThat()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("errors[0].message", equalTo(expected))
    }

    @Test
    fun readProducts() {
        val pageNumber = 0
        val pageSize = 10
        val sortBy = "name"
        val response = controller.getProducts(pageNumber = pageNumber, pageSize = pageSize, sortBy = sortBy)
        assertThat(response.body?.size).isEqualTo(pageSize)
        assertThat(response.statusCode.value()).isEqualTo(HttpStatus.OK.value())
    }

    @Test
    fun `getProducts() - return OK for pagination`() {
        val pageNumber = 0
        val pageSize = 10
        val sortBy = "name"
        RestAssured
            .given().log().all()
            .contentType(ContentType.JSON)
            .`when`().get("/api/products?pageNumber=$pageNumber&pageSize=$pageSize&sortBy=$sortBy")
            .then().log().all()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
    }

    @Test
    fun `getProducts() - return number of product per page size`() {
        val pageNumber = 0
        val pageSize = 5
        val sortBy = "name"
        val response =
            RestAssured
                .given().log().all()
                .contentType(ContentType.JSON)
                .`when`().get("/api/products?pageNumber=$pageNumber&pageSize=$pageSize&sortBy=$sortBy")
                .then().log().all().extract()

        val actual = response.jsonPath().getList<Map<String, Any>>("content")
        assertThat(actual).hasSize(pageSize)
    }

    @Test
    fun `getProducts() - should throw an exception for pagination`() {
        val pageNumber = 0
        val pageSize = 0
        val sortBy = "name"
        RestAssured
            .given().log().all()
            .contentType(ContentType.JSON)
            .`when`().get("/api/products?pageNumber=$pageNumber&pageSize=$pageSize&sortBy=$sortBy")
            .then().log().all()
            .assertThat()
            .statusCode(HttpStatus.BAD_REQUEST.value())
    }

    @Test
    fun readProduct() {
        val product = productRepository.findAll().first()
        val response = controller.getProduct(product.id)
        assertThat(response.statusCode.value()).isEqualTo(HttpStatus.OK.value())
    }

    @Test
    fun `readProduct() - but product doesn't exist`() {
        val id = 100
        RestAssured
            .given().log().all()
            .contentType(ContentType.JSON)
            .`when`().get("/api/products/$id")
            .then().log().all()
            .assertThat()
            .statusCode(HttpStatus.NOT_FOUND.value())
    }

    @Test
    fun `readProduct() - unit test, throw exception if product doesn't exist, `() {
        assertThrows<NotFoundException> { controller.getProduct(10) }
    }

    @Test
    fun update() {
        val product = productRepository.findAll().first()
        val newProductForm =
            ProductForm(name = "new product", price = 1.6, imageUrl = "https://www.product.com/image/2")
        val response = controller.updateProduct(product.id, newProductForm)
        assertThat(response.statusCode.value()).isEqualTo(HttpStatus.OK.value())
        val actual = response.body
        assertThat(actual?.id).isEqualTo(product.id)
        assertThat(actual?.name).isEqualTo(newProductForm.name)
        assertThat(actual?.price).isEqualTo(newProductForm.price)
        assertThat(actual?.imageUrl).isEqualTo(newProductForm.imageUrl)
    }

    @Test
    fun `update() - should return 400 when name is blank`() {
        val product = productRepository.findAll().first()
        val targetId = product.id
        val name = ""
        val targets =
            listOf(
                "Product name is required",
                "Contains unallowed character",
                "Must be no more than 15 characters, including spaces",
            )
        val response =
            RestAssured
                .given().log().all()
                .body(ProductForm(name = name, price = 1.5, imageUrl = "https://www.product.com/image/1"))
                .contentType(ContentType.JSON)
                .`when`().put("/api/products/$targetId")
                .then().log().all()
                .extract()

        val actualErrorMessage = response.jsonPath().getString("errors[0].message")
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        assertThat(actualErrorMessage).isIn(targets)
    }

    @Test
    fun `update() - should return 400 when price is 0`() {
        val product = productRepository.findAll().first()
        val targetId = product.id
        val expected = "Product price must be greater than zero"
        RestAssured
            .given().log().all()
            .body(ProductForm(name = "base", price = 0.0, imageUrl = "https://www.product.com/image/1"))
            .contentType(ContentType.JSON)
            .`when`().put("/api/products/$targetId")
            .then().log().all()
            .assertThat()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("errors[0].message", equalTo(expected))
    }

    @Test
    fun `update() - should return 400 when image URL is not valid`() {
        val product = productRepository.findAll().first()
        val targetId = product.id
        val expected = "Must start with 'https://'."
        RestAssured
            .given().log().all()
            .body(ProductForm(name = "base", price = 2.0, imageUrl = "ssh://www.product.com/image/1"))
            .contentType(ContentType.JSON)
            .`when`().put("/api/products/$targetId")
            .then().log().all()
            .assertThat()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("errors[0].message", equalTo(expected))
    }

    @Test
    fun `update() - should return 400 when name of product already exists`() {
        val name = "Superman"
        val product = productRepository.findByName(name).get()
        val name2 = "Man"
        val expected = "Product with name '$name2' already exists."

        val targetId = product.id

        RestAssured
            .given().log().all()
            .body(ProductForm(name = name2, price = 2.0, imageUrl = "https://www.product.com/image/1"))
            .contentType(ContentType.JSON)
            .`when`().put("/api/products/$targetId")
            .then().log().all()
            .assertThat()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("errors[0].message", equalTo(expected))
    }

    @Test
    fun delete() {
        val product = productRepository.findAll().first()
        val response = controller.deleteProduct(product.id)
        assertThat(response.statusCode.value()).isEqualTo(HttpStatus.NO_CONTENT.value())
    }

    @Test
    fun `delete() - should return 404 when the product to delete doesn't exist`() {
        val id = 100
        RestAssured
            .given().log().all()
            .contentType(ContentType.JSON)
            .`when`().delete("/api/products/$id")
            .then().log().all()
            .assertThat()
            .statusCode(HttpStatus.NOT_FOUND.value())
    }

    @Test
    fun `delete() - unit test, should return 404 when the product to delete doesn't exist`() {
        assertThrows<NotFoundException> { controller.deleteProduct(1000) }
    }
}
