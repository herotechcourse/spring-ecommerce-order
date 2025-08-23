package ecommerce.service

import ecommerce.dto.CartRequest
import ecommerce.entity.Cart
import ecommerce.entity.Member
import ecommerce.entity.Option
import ecommerce.repository.CartJpaRepository
import ecommerce.repository.CartStaticsRepository
import ecommerce.repository.OptionJpaRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class CartServiceUnitTest {
    @Mock
    lateinit var cartRepository: CartJpaRepository

    @Mock
    lateinit var cartStaticsRepository: CartStaticsRepository

    @Mock
    lateinit var optionRepository: OptionJpaRepository

    @Captor
    lateinit var cartCaptor: ArgumentCaptor<Cart>

    private lateinit var cartService: CartService

    private lateinit var member: Member
    private lateinit var option: Option

    @BeforeEach
    fun setUp() {
        cartService = CartService(cartRepository, cartStaticsRepository, optionRepository)
        member = Member(id = 1L, email = "user@test.com", password = "pw")
        option = Option("Size M", id = 10L, quantity = 1)
    }

    @Test
    fun `addOptionToCart should create new cart when member has none`() {
        `when`(optionRepository.findByProductIdAndId(100L, 10L)).thenReturn(option)
        `when`(cartRepository.findByMemberId(1L)).thenReturn(null)

        val request = CartRequest(productId = 100L, optionId = 10L, quantity = 2)

        cartService.addOptionToCart(member, request)

        verify(cartRepository).save(cartCaptor.capture())
        val savedCart = cartCaptor.value
        assertEquals(member, savedCart.member)
        assertEquals(1, savedCart.items.size)
        assertEquals(2, savedCart.items.first().quantity)
    }

    @Test
    fun `addOptionToCart should add quantity to existing item if already present`() {
        val existingCart = Cart(member)
        existingCart.add(option, 1)

        `when`(optionRepository.findByProductIdAndId(100L, 10L)).thenReturn(option)
        `when`(cartRepository.findByMemberId(1L)).thenReturn(existingCart)

        val request = CartRequest(productId = 100L, optionId = 10L, quantity = 3)

        cartService.addOptionToCart(member, request)

        assertEquals(4, existingCart.items.first().quantity)
        verify(cartRepository).save(existingCart)
    }

    @Test
    fun `getCartItems throws when cart not found`() {
        `when`(cartRepository.findByMemberId(1L)).thenReturn(null)

        assertThrows<NoSuchElementException> {
            cartService.getCartItems(1L)
        }
    }

    @Test
    fun `removeOptionFromCart removes items and updates quantity`() {
        val existingCart = Cart(member)
        existingCart.add(option, 5)

        `when`(cartRepository.findByMemberId(1L)).thenReturn(existingCart)
        val request = CartRequest(productId = 100L, optionId = 10L, quantity = 3)

        cartService.removeOptionFromCart(member, request)

        assertEquals(2, existingCart.items.first().quantity)
    }

    @Test
    fun `clearCart empties cart`() {
        val cart = Cart(member)
        cart.add(option, 1)
        `when`(cartRepository.findByMemberId(1L)).thenReturn(cart)

        cartService.clearCart(member)

        assertTrue(cart.items.isEmpty())
    }
}
