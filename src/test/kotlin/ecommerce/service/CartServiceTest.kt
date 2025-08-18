package ecommerce.service

import ecommerce.DatabaseFixture.BRUSH
import ecommerce.DatabaseFixture.createAdmin
import ecommerce.DatabaseFixture.createMina
import ecommerce.DatabaseFixture.createPaintingHappyHuman
import ecommerce.DatabaseFixture.createPaintingSadHuman
import ecommerce.DatabaseFixture.createPetra
import ecommerce.dto.CartItemRequest
import ecommerce.model.Option
import ecommerce.repository.MemberRepository
import ecommerce.repository.OptionRepository
import ecommerce.repository.ProductRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@DataJpaTest
@Import(CartService::class)
class CartServiceTest(
    @Autowired private val optionRepository: OptionRepository,
) {
    @Autowired
    private lateinit var cartService: CartService

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Test
    fun `adding item does not throw exception`() {
        assertDoesNotThrow {
            val member = memberRepository.save(createPetra())
            val product = productRepository.save(BRUSH)
            val option = optionRepository.save(Option("S", 10))
            product.addOption(option)
            val request = CartItemRequest(option.id, 1)

            val cartItem = cartService.addItem(member.id!!, request)
            cartItem.cart
        }
    }

    @Test
    fun `adding item returns the correct cart item`() {
        val member = memberRepository.save(createMina())
        val product = productRepository.save(createPaintingSadHuman())
        val option = optionRepository.save(Option("S", 10))
        product.addOption(option)
        val request = CartItemRequest(option.id, 1)

        val cartItem = cartService.addItem(member.id!!, request)
        assertThat(cartItem.option.name).isEqualTo(option.name)
    }

    @Test
    fun `delete item does not throw an exception`() {
        val member = memberRepository.save(createAdmin())
        val memberId = member.id!!
        val product = productRepository.save(createPaintingHappyHuman())
        val option = optionRepository.save(Option("S", 10))
        product.addOption(option)
        val addRequest = CartItemRequest(option.id, 1)
        val cartItem = cartService.addItem(memberId, addRequest)

        val deleteRequest = CartItemRequest(cartItem.option.id, 1)

        assertDoesNotThrow {
            cartService.deleteItem(memberId, deleteRequest)
        }
    }
}
