package ecommerce.service

import ecommerce.dto.cart.AddToCartRequest
import ecommerce.exception.NotFoundException
import ecommerce.model.Cart
import ecommerce.model.CartItem
import ecommerce.model.Member
import ecommerce.model.Product
import ecommerce.model.ProductOption
import ecommerce.model.Role
import ecommerce.repository.CartItemRepository
import ecommerce.repository.CartRepository
import ecommerce.repository.CartStatisticsRepository
import ecommerce.repository.ProductOptionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.time.LocalDateTime
import java.util.Optional

class CartItemServiceTest {
    @Mock
    private lateinit var cartRepository: CartRepository

    @Mock
    private lateinit var cartItemRepository: CartItemRepository

    @Mock
    private lateinit var productOptionRepository: ProductOptionRepository

    @Mock
    private lateinit var cartStatisticsRepository: CartStatisticsRepository

    private lateinit var cartItemService: CartItemService

    private lateinit var testMember: Member
    private lateinit var testCart: Cart
    private lateinit var testProduct: Product
    private lateinit var testProductOption: ProductOption
    private lateinit var testCartItem: CartItem

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        cartItemService =
            CartItemService(
                cartRepository,
                cartItemRepository,
                productOptionRepository,
                cartStatisticsRepository,
            )
        testMember = Member("test@example.com", "password", "Test User", Role.USER, id = 1L)
        testCart = Cart(member = testMember, id = 1L)
        testProduct = Product("Test Product", 99.99, 10, "https://example.com/image.jpg", id = 1L)
        testProductOption = ProductOption("Red", 5, testProduct, price = 30.0, id = 1L)
        testCartItem = CartItem(testCart, testProductOption, 2, LocalDateTime.now(), id = 1L)
    }

    @Test
    fun `addCartItem should create new cart item successfully`() {
        val request = AddToCartRequest(1L, 3, 1L, 1L)
        val cartId = 1L

        `when`(cartRepository.findById(cartId)).thenReturn(Optional.of(testCart))
        `when`(productOptionRepository.findById(request.productOptionId)).thenReturn(Optional.of(testProductOption))
        `when`(cartItemRepository.save(any(CartItem::class.java))).thenReturn(testCartItem)

        val result = cartItemService.addCartItem(request, cartId)

        assertThat(result).isNotNull
        verify(cartRepository).findById(cartId)
        verify(productOptionRepository).findById(request.productOptionId)
        verify(cartItemRepository).save(any(CartItem::class.java))
    }

    @Test
    fun `addCartItem should throw NotFoundException when cart not found`() {
        val request = AddToCartRequest(1L, 3, 1L, 1L)
        val cartId = 999L

        `when`(cartRepository.findById(cartId)).thenReturn(Optional.empty())

        assertThrows(NotFoundException::class.java) {
            cartItemService.addCartItem(request, cartId)
        }
    }

    @Test
    fun `deleteCartItemById should delete cart item successfully`() {
        val cartItemId = 1L
        val cartId = 1L

        `when`(cartRepository.findById(cartId)).thenReturn(Optional.of(testCart))
        `when`(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(testCartItem))

        cartItemService.deleteCartItemById(cartItemId, cartId)

        verify(cartItemRepository).deleteById(cartItemId)
    }
}
