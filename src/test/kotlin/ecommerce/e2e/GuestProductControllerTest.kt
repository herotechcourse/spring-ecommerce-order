package ecommerce.e2e

import io.restassured.RestAssured
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
class GuestProductControllerTest {
    @Test
    fun getProducts() {
        val response =
            RestAssured
                .given().log().all()
                .`when`()
                .request("GET", "/api/products")
                .then()
                .extract()
                .response()

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK.value())
        val products = JSONObject(response.asString())
        assertThat(products.get("totalElements")).isEqualTo(10)
    }

    @Test
    fun getProduct() {
        val response =
            RestAssured
                .given().log().all()
                .`when`()
                .request("GET", "/api/products/1")
                .then()
                .extract()
                .response()

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.OK.value())
        val productName = response.body().jsonPath().getString("name")
        Assertions.assertThat(productName).isEqualTo("Espresso")
    }

    @Test
    fun `should respond with 404 if product not found`() {
        val response =
            RestAssured
                .given().log().all()
                .`when`()
                .request("GET", "/api/products/100")
                .then()
                .extract()
                .response()

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND.value())
    }
}
