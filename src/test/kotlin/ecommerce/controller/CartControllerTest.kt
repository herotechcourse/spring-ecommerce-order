package ecommerce.controller

import com.fasterxml.jackson.databind.ObjectMapper
import ecommerce.dto.CartItem
import ecommerce.dto.CartRequest
import ecommerce.dto.MemberResponse
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

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var cartService: CartService

    @MockitoBean
    private lateinit var jwtProvider: JWTProvider

    @MockitoBean
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val token = "mocked-jwt-token"
    private val memberResponse =
        MemberResponse(id = 1L, email = "user@example.com", name = "John Doe", role = UserRole.USER.name)
    private val cartRequest = CartRequest(productId = 100L)

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

        verify(cartService).addToCart(memberResponse.id, cartRequest.productId)
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

        verify(cartService).removeFromCart(memberResponse.id, cartRequest.productId)
    }

    @Test
    fun `should return cart items`() {
        val cartItems =
            listOf(
                CartItem(productId = 1L, name = "Item1", price = 500.0, quantity = 2),
                CartItem(productId = 2L, name = "Item2", price = 1000.0, quantity = 1),
            )

        `when`(cartService.getCartItems(memberResponse.id)).thenReturn(cartItems)

        mockMvc.perform(
            get("/api/protected/cart")
                .header("Authorization", "Bearer $token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.size()").value(2))
            .andExpect(jsonPath("$[0].productId").value(1))
            .andExpect(jsonPath("$[0].name").value("Item1"))
            .andExpect(jsonPath("$[0].price").value(500))
            .andExpect(jsonPath("$[0].quantity").value(2))
            .andExpect(jsonPath("$[1].productId").value(2))

        verify(cartService).getCartItems(memberResponse.id)
    }
}
