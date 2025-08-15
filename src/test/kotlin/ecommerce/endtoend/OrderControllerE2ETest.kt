package ecommerce.endtoend

import ecommerce.dto.OptionDTO
import ecommerce.dto.OrderDTO
import ecommerce.dto.ProductRequestDTO
import ecommerce.dto.ProductResponseDTO
import ecommerce.dto.TokenRequestDTO
import ecommerce.entities.CartItemEntity
import ecommerce.entities.MemberEntity
import ecommerce.mappers.toEntity
import ecommerce.model.PaymentRequest
import ecommerce.repositories.CartItemRepository
import ecommerce.repositories.MemberRepository
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class OrderControllerE2ETest(
    @param:Autowired
    var cartItemRepository: CartItemRepository,
    @param:Autowired
    var memberRepository: MemberRepository,
) {
    private lateinit var token: String
    private var memberId: Long = 0L
    private lateinit var member: MemberEntity
    private var optionId: Long = 0L

    @BeforeEach
    fun loginAndPrepareData() {
        val loginPayload =
            TokenRequestDTO(
                "sebas@sebas.com",
                "123456",
            )
        val response =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .post("/api/members/login")
                .then().extract()

        token = response.body().jsonPath().getString("accessToken")
        Assertions.assertThat(token).isNotBlank

        member = memberRepository.findByEmail(loginPayload.email)!!
        memberId = member.id
    }

    @Test
    fun `create order success`() {
        createCartItem()
        val paymentRequest =
            PaymentRequest(
                amount = 100.0,
                currency = "eur",
                paymentMethod = "pm_card_visa",
            )

        val response =
            RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(paymentRequest)
                .post("/api/order")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().`as`(OrderDTO::class.java)

        Assertions.assertThat(response).isNotNull
        Assertions.assertThat(response.totalAmount).isEqualTo(1000.0)
        Assertions.assertThat(response.status.toString()).isEqualTo("CREATED")
    }

    @Test
    fun `create order fails with empty cart`() {
        val carts = cartItemRepository.findByMemberId(memberId)
        cartItemRepository.deleteAll(carts)

        val paymentRequest =
            PaymentRequest(
                amount = 100.0,
                currency = "eur",
                paymentMethod = "pm_card_visa",
            )

        RestAssured.given()
            .auth().oauth2(token)
            .contentType(ContentType.JSON)
            .body(paymentRequest)
            .post("/api/orders")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
    }

    private fun createCartItem() {
        val productDTO =
            ProductRequestDTO(
                name = "Test Product",
                price = 100.0,
                imageUrl = "https://example.com/image.jpg",
                options =
                    setOf(
                        OptionDTO(
                            name = "Default Option",
                            quantity = 10,
                            productId = null,
                            unitPrice = 100.0,
                        ),
                    ),
            )

        val productResponse =
            RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(productDTO)
                .post("/api/products")
                .then().statusCode(HttpStatus.CREATED.value())
                .extract().`as`(ProductResponseDTO::class.java)

        val options = productResponse.options
        optionId = options[0].id

        val cartItem =
            CartItemEntity(
                member = member,
                option = options[0].toEntity(productResponse.toEntity()),
                quantity = 2,
                addedAt = LocalDateTime.now(),
            )

        cartItemRepository.save(cartItem)
    }
}
