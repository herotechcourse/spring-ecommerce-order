package ecommerce.integration

import ecommerce.dto.MemberLoginDTO
import ecommerce.dto.OrderDTO
import ecommerce.entities.CartItemEntity
import ecommerce.entities.MemberEntity
import ecommerce.entities.OptionEntity
import ecommerce.entities.OrderEntity
import ecommerce.exception.NotFoundException
import ecommerce.exception.OperationFailedException
import ecommerce.model.PaymentRequest
import ecommerce.repositories.CartItemRepository
import ecommerce.repositories.MemberRepository
import ecommerce.repositories.OptionRepository
import ecommerce.repositories.OrderRepository
import ecommerce.services.order.OrderServiceImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@Transactional
class OrderServiceTest {
    @Autowired
    lateinit var orderService: OrderServiceImpl

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Autowired
    lateinit var cartItemRepository: CartItemRepository

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Autowired
    lateinit var optionRepository: OptionRepository

    lateinit var member: MemberEntity
    lateinit var option: OptionEntity
    lateinit var cartItem: CartItemEntity

    @BeforeEach
    fun setup() {
        // Create and save a member
        val members = memberRepository.findAll()
        member = members[0]

        val options = optionRepository.findAll()
        option = options[0]

        // Add an item to the member's cart
        val cartItemEntity =
            CartItemEntity(
                member = member,
                option = option,
                quantity = 2,
                addedAt = LocalDateTime.now(),
            )

        cartItem = cartItemRepository.save(cartItemEntity)
    }

    @Test
    fun `create should create order, payment, reduce stock and clear cart`() {
        val memberLoginDTO = MemberLoginDTO(id = member.id)
        val paymentRequest =
            PaymentRequest(
                amount = 2000.0,
                currency = "eur",
                paymentMethod = "pm_card_visa",
            )

        val orderDTO: OrderDTO = orderService.create(memberLoginDTO, paymentRequest)

        assertNotNull(orderDTO.id)
        assertEquals(6000.0, orderDTO.totalAmount)
        assertEquals(member.id, orderDTO.memberId)

        val order = orderRepository.findByIdOrNull(orderDTO.id!!)
        assertNotNull(order)
        assertEquals(OrderEntity.OrderStatus.CREATED, order.status)

        assertEquals(1, order.items.size)
        assertEquals(option.id, order.items[0].option.id)
        assertEquals(2, order.items[0].quantity)

        val updatedOption = optionRepository.findByIdOrNull(option.id)
        assertNotNull(updatedOption)
        assertEquals(3, updatedOption.quantity) // 10 - 2 = 8

        val cartItemsAfter = cartItemRepository.findByMemberId(member.id)
        assertTrue(cartItemsAfter.isEmpty())
    }

    @Test
    fun `create should throw if member not found`() {
        val invalidMemberDTO = MemberLoginDTO(id = 999999L) // non-existent id
        val paymentRequest =
            PaymentRequest(
                amount = 1000.0,
                currency = "eur",
                paymentMethod = "pm_card_visa",
            )

        assertThrows<NotFoundException> {
            orderService.create(invalidMemberDTO, paymentRequest)
        }
    }

    @Test
    fun `create should throw if cart is empty`() {
        cartItemRepository.deleteById(cartItem.id)

        val memberLoginDTO = MemberLoginDTO(id = member.id)
        val paymentRequest =
            PaymentRequest(
                amount = 1000.0,
                currency = "eur",
                paymentMethod = "pm_card_visa",
            )

        assertThrows<OperationFailedException> {
            orderService.create(memberLoginDTO, paymentRequest)
        }
    }
}
