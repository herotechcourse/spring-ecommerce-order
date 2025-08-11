package ecommerce.service

import ecommerce.dto.OrderPlaceForm
import ecommerce.exception.EmptyCartException
import ecommerce.exception.InsufficientStockException
import ecommerce.model.Order
import ecommerce.model.OrderStatus
import ecommerce.repository.CartItemRepository
import ecommerce.repository.MemberRepository
import ecommerce.repository.OptionRepository
import ecommerce.repository.OrderItemRepository
import ecommerce.repository.OrderRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class OrderServiceTest(
    @Autowired private val orderService: OrderService,
    @Autowired private val orderPaymentService: OrderPaymentService,
    @Autowired private val memberRepository: MemberRepository,
    @Autowired private val cartItemRepository: CartItemRepository,
    @Autowired private val optionRepository: OptionRepository,
    @Autowired private val orderItemRepository: OrderItemRepository,
    @Autowired private val orderRepository: OrderRepository,
) {
    @Test
    fun readOrders() {
        val member = memberRepository.findAll().first()
        val cartItemIds1 = listOf(1L)
        val cartItemIds2 = listOf(2L)

        orderService.placeOrder(member.id, OrderPlaceForm(cartItemIds1))
        orderService.placeOrder(member.id, OrderPlaceForm(cartItemIds2))

        val orders = orderService.readOrders(member.id)

        assertThat(orders).hasSize(2)
    }

    @Test
    fun readOrder() {
        val member = memberRepository.findAll().first()
        val cartItemId = 1L
        val savedOrder: Order = orderService.placeOrder(member.id, OrderPlaceForm(listOf(cartItemId)))

        val order = orderService.readOrder(savedOrder.id, member.id)

        assertThat(order).isNotNull()
        assertThat(order.orderItems).hasSize(1)
    }

    @Test
    fun `placeOrder() - return order when order processed well`() {
        val member = memberRepository.findAll().first()
        val cartItemId = 2L
        val cartItem = cartItemRepository.findById(cartItemId).get()
        val optionQuantityPre = cartItem.option.quantity

        val savedOrder: Order = orderService.placeOrder(member.id, OrderPlaceForm(listOf(cartItemId)))

        val savedOrderItem = savedOrder.orderItems.first()
        val optionId = savedOrderItem.option.id
        val option = optionRepository.findById(optionId).get()
        val optionQuantityPost = option.quantity

        assertThat(savedOrder.id).isNotNull()
        assertThat(cartItemRepository.findById(cartItemId).getOrNull()).isNull()
        assertThat(optionQuantityPre - savedOrderItem.quantity).isEqualTo(optionQuantityPost)
    }

    @Test
    fun `placeOrder() - throw exception when cart is empty`() {
        val member = memberRepository.findAll().first()
        assertThrows<EmptyCartException> { orderService.placeOrder(member.id, OrderPlaceForm(listOf())) }
    }

    @Test
    fun `placeOrder() - throw exception when the stock is insufficient`() {
        val member = memberRepository.findAll().first()
        assertThrows<InsufficientStockException> { orderService.placeOrder(member.id, OrderPlaceForm(listOf(15L))) }
    }

    @Test
    fun `placeOrder() - payment error`() {
        val member = memberRepository.findAll().first()
        assertThrows<InsufficientStockException> { orderService.placeOrder(member.id, OrderPlaceForm(listOf(15L))) }
    }

    @Test
    fun `placeOrder() - payment integration test for success`() {
        val cartItemIds = listOf(5L, 8L)
        val member = memberRepository.findAll().first()
        val savedOrder = orderService.placeOrder(member.id, OrderPlaceForm(cartItemIds, "pm_card_visa"))

        val actual = orderRepository.findByIdOrNull(savedOrder.id)

        assertThat(actual).isNotNull()
        assertThat(actual?.status).isEqualTo(OrderStatus.ORDERED)
    }

    @Test
    fun `placeOrder() - payment integration test for fail`() {
        val cartItemIds = listOf(11L)
        val member = memberRepository.findAll().first()
        assertThrows<IllegalArgumentException> {
            orderService.placeOrder(
                member.id,
                OrderPlaceForm(cartItemIds, "pm_card_visa_chargeDeclined"),
            )
        }
    }
}
