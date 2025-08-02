package ecommerce.controller

import ecommerce.dto.ProductRequest
import ecommerce.dto.OptionRequest
import ecommerce.repository.ProductJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductRestControllerJpaTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var productRepository: ProductJpaRepository

    @Test
    fun `create and fetch product`() {
        val request = ProductRequest(
            name = "chair",
            price = 22.0,
            imageUrl = "https://test.com/chair.jpg",
            options = listOf(OptionRequest(name = "default", quantity = 1))
        )

        val postResponse: ResponseEntity<Map<String, Any>> = restTemplate.postForEntity(
            "/api/products",
            request,
            object : ParameterizedTypeReference<Map<String, Any>>() {}
        )

        assertThat(postResponse.statusCode).isEqualTo(HttpStatus.OK)
        val productId = postResponse.body?.get("id") as Int

        val getResponse: ResponseEntity<Map<String, Any>> = restTemplate.getForEntity(
            "/api/products/$productId",
            object : ParameterizedTypeReference<Map<String, Any>>() {}
        )

        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(getResponse.body?.get("name")).isEqualTo("chair")
    }
}
