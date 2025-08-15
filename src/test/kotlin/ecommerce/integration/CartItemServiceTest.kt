package ecommerce.integration

import ecommerce.controller.cart.usecase.ManageCartItemUseCase
import ecommerce.dto.CartItemRequestDTO
import ecommerce.entities.OptionEntity
import ecommerce.entities.ProductEntity
import ecommerce.mappers.toDTO
import ecommerce.repositories.MemberRepository
import ecommerce.repositories.OptionRepository
import ecommerce.repositories.ProductRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional

@Transactional
@SpringBootTest
class CartItemServiceTest {
    @Autowired
    private lateinit var cartItemService: ManageCartItemUseCase

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var optionRepository: OptionRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    private var optionId: Long = 0
    private val memberId = 1L

    @BeforeEach
    fun setup() {
        val productEntity =
            ProductEntity(
                name = "Keyboard",
                price = 50.0,
                imageUrl = "keyboard.png",
            )

        val product = productRepository.save(productEntity)
        optionId =
            optionRepository.save(
                OptionEntity(
                    name = "Mechanical Keyboard",
                    quantity = 2,
                    product = product,
                    unitPrice = 100.0,
                ),
            ).id
    }

    @Test
    fun `addOrUpdate should create new cart item if not exists`() {
        val dto = CartItemRequestDTO(optionId = optionId, quantity = 2)
        val member = memberRepository.findByIdOrNull(memberId)?.toDTO()

        val response = cartItemService.addOrUpdate(dto, member?.id!!)

        assertThat(response.id).isNotNull()
        assertThat(response.quantity).isEqualTo(2)
        assertThat(response.option.name).isEqualTo("Mechanical Keyboard")
    }

    @Test
    fun `addOrUpdate should update quantity if item exists`() {
        val initial = CartItemRequestDTO(optionId = optionId, quantity = 1)
        val member = memberRepository.findByIdOrNull(memberId)?.toDTO()
        cartItemService.addOrUpdate(initial, member?.id!!)

        val updated = CartItemRequestDTO(optionId = optionId, quantity = 5)
        val result = cartItemService.addOrUpdate(updated, member.id)

        assertThat(result.quantity).isEqualTo(5)
    }

    @Test
    fun `addOrUpdate should throw if product not found`() {
        val badDto = CartItemRequestDTO(optionId = 9999L, quantity = 1)
        val member = memberRepository.findByIdOrNull(memberId)?.toDTO()

        assertThrows<EmptyResultDataAccessException> {
            cartItemService.addOrUpdate(badDto, member?.id!!)
        }
    }

    @Test
    fun `findByMember should return cart items for a member`() {
        val member = memberRepository.findByIdOrNull(memberId)?.toDTO()
        cartItemService.addOrUpdate(CartItemRequestDTO(optionId, 1), member?.id!!)

        val items = cartItemService.findByMember(memberId)

        assertThat(items).hasSize(1)
        assertThat(items[0].option.id).isEqualTo(optionId)
        assertThat(items[0].quantity).isEqualTo(1)
    }

    @Test
    fun `delete should remove cart item for member`() {
        val member = memberRepository.findByIdOrNull(memberId)?.toDTO()
        cartItemService.addOrUpdate(CartItemRequestDTO(optionId, 2), member?.id!!)

        cartItemService.delete(CartItemRequestDTO(optionId, 2), memberId)

        val items = cartItemService.findByMember(memberId)
        assertThat(items).isEmpty()
    }
}
