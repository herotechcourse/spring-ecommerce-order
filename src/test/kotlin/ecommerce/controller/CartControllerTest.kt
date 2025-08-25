package ecommerce.controller

import com.fasterxml.jackson.databind.ObjectMapper
import ecommerce.dto.CartRequest
import ecommerce.dto.MemberResponse
import ecommerce.entity.CartEntity
import ecommerce.entity.CartItemEntity
import ecommerce.entity.OptionEntity
import ecommerce.entity.ProductEntity
import ecommerce.infrastructure.JWTProvider
import ecommerce.model.UserRole
import ecommerce.service.AuthService
import ecommerce.service.CartService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var cartService: CartService

    @MockitoBean
    private lateinit var cartEntity: CartEntity

    @MockitoBean
    private lateinit var jwtProvider: JWTProvider

    @MockitoBean
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val token = "mocked-jwt-token"
    private val memberResponse =
        MemberResponse(id = 1L, email = "user@example.com", name = "John Doe", role = UserRole.USER.name)
    private val cartRequest = CartRequest(productOptionId = 100L)

    @BeforeEach
    fun setup() {
        doNothing().`when`(jwtProvider).validateToken(token)
        `when`(authService.findMemberByToken(token)).thenReturn(memberResponse)
    }

    @Test
    fun `should add product to cart`() {
        mockMvc.perform(
            post("/api/protected/cart")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartRequest)),
        )
            .andExpect(status().isCreated)

        verify(cartService).addToCart(memberResponse.id, cartRequest.productOptionId)
    }

    @Test
    fun `should remove product from cart`() {
        mockMvc.perform(
            delete("/api/protected/cart")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartRequest)),
        )
            .andExpect(status().isNoContent)

        verify(cartService).removeFromCart(memberResponse.id, cartRequest.productOptionId)
    }

    @Test
    fun `should return cart items`() {
        val cart = CartEntity(1L, 1L, LocalDateTime.now())
        val option1 = OptionEntity(1L, "Small", 1)
        val option2 = OptionEntity(1L, "Large", 1)
        val product = ProductEntity(1L, "T-Shirt", 9.99, "https://example.png", mutableListOf(option1, option2))
        option1.product = product
        option2.product = product

        val localDateTime = LocalDateTime.now()
        val cartItems =
            listOf(
                CartItemEntity(
                    cart = cart,
                    product = product,
                    productOption = option1,
                    quantity = 2,
                    createdAt = localDateTime,
                ),
                CartItemEntity(
                    cart = cart,
                    product = product,
                    productOption = option2,
                    quantity = 1,
                    createdAt = localDateTime,
                ),
            )

        `when`(cartService.getCartItems(memberResponse.id)).thenReturn(cartItems)

        mockMvc.perform(
            get("/api/protected/cart")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.size()").value(2))
            .andExpect(jsonPath("$[0].product.id").value(1))
            .andExpect(jsonPath("$[0].product.name").value("T-Shirt"))
            .andExpect(jsonPath("$[0].productOption.name").value("Small"))
            .andExpect(jsonPath("$[0].quantity").value(2))
            .andExpect(jsonPath("$[1].product.id").value(1))
            .andExpect(jsonPath("$[1].product.name").value("T-Shirt"))
            .andExpect(jsonPath("$[1].productOption.name").value("Large"))
            .andExpect(jsonPath("$[1].quantity").value(1))

        verify(cartService).getCartItems(memberResponse.id)
    }
}
