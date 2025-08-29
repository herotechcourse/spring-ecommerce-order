package ecommerce.controller

import com.fasterxml.jackson.databind.ObjectMapper
import ecommerce.dto.order.CreateOrderRequest
import ecommerce.model.Currency
import ecommerce.model.Member
import ecommerce.model.Role
import ecommerce.service.TokenService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class CheckoutControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var tokenService: TokenService

    private lateinit var userToken: String

    @BeforeEach
    fun setUp() {
        val userMember = Member("user@test.com", "password", "Test User", Role.USER, id = 1L)
        userToken = tokenService.generateToken(userMember)
    }

    @Test
    fun `should return 401 when not authenticated`() {
        val request =
            CreateOrderRequest(
                cartItemIds = listOf(1L),
                currency = Currency.EUR,
            )

        mockMvc.post("/api/checkout") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `should return 400 for empty cart items`() {
        val request =
            CreateOrderRequest(
                cartItemIds = emptyList(),
                currency = Currency.EUR,
            )

        mockMvc.post("/api/checkout") {
            header("Authorization", "Bearer $userToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `should return 404 for non-existent cart items`() {
        val request =
            CreateOrderRequest(
                cartItemIds = listOf(999999L),
                currency = Currency.EUR,
            )

        mockMvc.post("/api/checkout") {
            header("Authorization", "Bearer $userToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNotFound() }
        }
    }
}
