import ecommerce.entity.CartItemEntity
import ecommerce.repository.CartItemRepositoryJpa
import ecommerce.repository.CartRepository
import ecommerce.repository.ProductRepositoryJpa
import ecommerce.service.CartService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.util.Optional

class CartServiceTest {
    private lateinit var cartRepository: CartRepository
    private lateinit var productRepositoryJpa: ProductRepositoryJpa
    private lateinit var cartItemRepositoryJpa: CartItemRepositoryJpa
    private lateinit var cartService: CartService

    @BeforeEach
    fun setup() {
        cartRepository = mock(CartRepository::class.java)
        productRepositoryJpa = mock(ProductRepositoryJpa::class.java)
        cartItemRepositoryJpa = mock(CartItemRepositoryJpa::class.java)

        cartService = CartService(cartRepository, productRepositoryJpa, cartItemRepositoryJpa)
    }

    @Test
    fun `addToCart calls cartRepository add when product exists`() {
        val productId = 1L
        val memberId = 1L

        `when`(productRepositoryJpa.findById(productId)).thenReturn(Optional.of(mock(ecommerce.entity.ProductEntity::class.java)))

        cartService.addToCart(memberId, productId)

        verify(cartRepository).add(memberId, productId)
    }

    @Test
    fun `addToCart throws when product not found`() {
        val productId = 1L
        val memberId = 1L
        `when`(productRepositoryJpa.findById(productId)).thenReturn(Optional.empty())

        val exception =
            assertThrows(NoSuchElementException::class.java) {
                cartService.addToCart(memberId, productId)
            }
        assertEquals("Product not found", exception.message)
    }

    @Test
    fun `getAllCartItems returns paginated cart items`() {
        val pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "created_at"))
        val cartItemEntity = mock(CartItemEntity::class.java)
        val page: Page<CartItemEntity> = PageImpl(listOf(cartItemEntity), pageRequest, 1)

        `when`(cartItemRepositoryJpa.findAll(pageRequest)).thenReturn(page)

        val result = cartService.getAllCartItems(0, 10)

        assertEquals(1, result.totalElements)
        verify(cartItemRepositoryJpa).findAll(pageRequest)
    }
}
