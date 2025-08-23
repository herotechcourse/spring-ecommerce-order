package ecommerce.service

import ecommerce.dto.CartRequest
import ecommerce.entity.Member
import ecommerce.entity.Option
import ecommerce.entity.Product
import ecommerce.repository.CartJpaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@AutoConfigureTestEntityManager
class CartServiceIntegrationTest {
    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var cartService: CartService

    @Autowired
    private lateinit var cartRepository: CartJpaRepository

    private lateinit var member: Member
    private lateinit var option: Option
    private lateinit var product: Product

    @BeforeEach
    fun setup() {
        member = Member(email = "user@test.com", password = "pw")
        entityManager.persist(member)

        option = Option(name = "Size M", quantity = 10)
        product = Product(name = "T-shirt", price = 1000, "http://test.png", listOf(option))
        entityManager.persist(product)
        entityManager.flush()
    }

    @Test
    fun `full cart flow persists correctly`() {
        val addRequest = CartRequest(productId = product.id, optionId = option.id, quantity = 2)
        cartService.addOptionToCart(member, addRequest)

        val items = cartService.getCartItems(member.id)
        assertEquals(1, items.size)
        assertEquals(2, items.first().quantity)

        val removeRequest = CartRequest(productId = product.id, optionId = option.id, quantity = 1)
        cartService.removeOptionFromCart(member, removeRequest)
        val updatedItems = cartService.getCartItems(member.id)
        assertEquals(1, updatedItems.first().quantity)

        cartService.clearCart(member.id)
        val cart = cartRepository.findByMemberId(member.id)
        assertNotNull(cart)
        assertTrue(cart.items.isEmpty())
    }
}
