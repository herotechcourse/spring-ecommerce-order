import ecommerce.entity.CartEntity
import ecommerce.entity.CartItemEntity
import ecommerce.entity.OptionEntity
import ecommerce.entity.ProductEntity
import ecommerce.repository.CartItemRepositoryJpa
import ecommerce.repository.CartRepository
import ecommerce.repository.CartRepositoryJpa
import ecommerce.repository.OptionRepositoryJpa
import ecommerce.repository.ProductRepositoryJpa
import ecommerce.service.CartService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
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
    private lateinit var cartRepositoryJpa: CartRepositoryJpa
    private lateinit var cartItemRepository: CartItemRepositoryJpa
    private lateinit var optionRepositoryJpa: OptionRepositoryJpa
    private lateinit var cartItemRepositoryJpa: CartItemRepositoryJpa
    private lateinit var cartService: CartService

    @BeforeEach
    fun setup() {
        cartRepository = mock(CartRepository::class.java)
        cartRepositoryJpa = mock(CartRepositoryJpa::class.java)
        cartItemRepository = mock(CartItemRepositoryJpa::class.java)
        optionRepositoryJpa = mock(OptionRepositoryJpa::class.java)
        cartItemRepositoryJpa = mock(CartItemRepositoryJpa::class.java)
        productRepositoryJpa = mock(ProductRepositoryJpa::class.java)

        cartService = CartService(cartRepository, cartRepositoryJpa, optionRepositoryJpa, cartItemRepositoryJpa)
    }

    @Test
    fun `addToCart calls cartRepository add when product exists`() {
        val memberId = 1L
        val productOptionId = 1L
        val productId = 10L

        val productOption =
            OptionEntity(
                id = productOptionId,
                name = "XL",
                quantity = 1,
                product = null,
            )

        val product =
            ProductEntity(
                id = productId,
                name = "Test Product",
                price = 99.99,
                imageUrl = "http://image.url",
                options = mutableListOf(productOption),
            )

        productOption.product = product

        `when`(optionRepositoryJpa.findById(productOptionId)).thenReturn(Optional.of(productOption))
        `when`(cartItemRepositoryJpa.findByCartIdAndProductOptionId(1L, productOptionId)).thenReturn(null)
        `when`(cartRepositoryJpa.save(any())).thenAnswer { invocation ->
            val cartArg = invocation.arguments[0] as CartEntity
            CartEntity(
                id = 1L,
                memberId = cartArg.memberId,
                createdAt = cartArg.createdAt,
            )
        }
        `when`(productRepositoryJpa.findById(productId)).thenReturn(Optional.of(mock(ecommerce.entity.ProductEntity::class.java)))

        cartService.addToCart(memberId, productOptionId)

        verify(optionRepositoryJpa).findById(productOptionId)
        verify(cartRepositoryJpa).findByMemberId(memberId)
        verify(cartRepositoryJpa).save(any())
        verify(cartItemRepositoryJpa).findByCartIdAndProductOptionId(1L, productOptionId)
        verify(cartItemRepositoryJpa).save(any())
    }

    @Test
    fun `addToCart throws when product option not found`() {
        val productOptionId = 1L
        val memberId = 1L

        `when`(optionRepositoryJpa.findById(productOptionId)).thenReturn(Optional.empty())

        val exception =
            assertThrows(NoSuchElementException::class.java) {
                cartService.addToCart(memberId, productOptionId) // Pass productOptionId here, not productId
            }

        assertEquals("Product Option not found", exception.message)
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
