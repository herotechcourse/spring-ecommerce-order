package ecommerce.service

import ecommerce.dto.OrderItemRequest
import ecommerce.dto.PaymentRequest
import ecommerce.dto.PlaceOrderRequest
import ecommerce.entity.Member
import ecommerce.entity.Option
import ecommerce.entity.Product
import ecommerce.entity.enumerated.PaymentAttemptStatus
import ecommerce.repository.OrderJpaRepository
import ecommerce.repository.PaymentAttemptJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException

@SpringBootTest
@Transactional
@AutoConfigureTestEntityManager
class OrderCreationServiceIntegrationTest {
    @Autowired
    private lateinit var orderCreationService: OrderCreationService

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var orderRepo: OrderJpaRepository

    @Autowired
    private lateinit var paymentAttemptRepo: PaymentAttemptJpaRepository

    private lateinit var member: Member
    private lateinit var option: Option
    private lateinit var product: Product
    private val initialStock = 10

    @BeforeEach
    fun setup() {
        member = Member(email = "order-user@test.com", password = "pw")
        entityManager.persist(member)

        option = Option(name = "L Size", quantity = initialStock)
        product = Product(name = "Hoodie", price = 3000, "http://test.png", listOf(option))
        entityManager.persist(product)

        entityManager.flush()
    }

    @Test
    @DisplayName("createPending() should successfully create an order, a payment, and decrease the product quantity")
    fun `should create order and payment attempt, and decrease stock on success`() {
        val quantityToOrder = 3
        val request =
            PlaceOrderRequest(
                orderItems = listOf(OrderItemRequest(optionId = option.id, quantity = quantityToOrder)),
                PaymentRequest(9000, "EUR", "pm_card_visa"),
            )

        val (createdOrder, paymentAttempt) = orderCreationService.createPending(member.id, request)

        assertThat(createdOrder.id).isNotNull()
        assertThat(createdOrder.totalAmount).isEqualTo(3000 * quantityToOrder)
        assertThat(createdOrder.member.id).isEqualTo(member.id)

        assertThat(paymentAttempt.id).isNotNull()
        assertThat(paymentAttempt.status).isEqualTo(PaymentAttemptStatus.PENDING)
        assertThat(paymentAttempt.order.id).isEqualTo(createdOrder.id)

        entityManager.flush()
        entityManager.clear()
        val updatedOption = entityManager.find(Option::class.java, option.id)
        assertThat(updatedOption.quantity).isEqualTo(initialStock - quantityToOrder)
    }

    @Test
    @DisplayName("createPending() should throw an exception when stock is insufficient")
    fun `should throw IllegalStateException when stock is insufficient`() {
        val quantityToOrder = initialStock + 1
        val request =
            PlaceOrderRequest(
                orderItems = listOf(OrderItemRequest(optionId = option.id, quantity = quantityToOrder)),
                PaymentRequest(9000, "EUR", "pm_card_visa"),
            )

        assertThrows<IllegalStateException> {
            orderCreationService.createPending(member.id, request)
        }

        assertThat(orderRepo.count()).isEqualTo(0)
        assertThat(paymentAttemptRepo.count()).isEqualTo(0)
    }

    @Test
    @DisplayName("createPending() should throw an exception for a non-existent memberId")
    fun `should throw exception for non-existent memberId`() {
        val nonExistentMemberId = 999L
        val request =
            PlaceOrderRequest(
                orderItems = listOf(OrderItemRequest(optionId = option.id, quantity = 1)),
                PaymentRequest(9000, "EUR", "pm_card_visa"),
            )

        assertThrows<NoSuchElementException> {
            orderCreationService.createPending(nonExistentMemberId, request)
        }
    }

    @Test
    @DisplayName("createPending() should throw an exception for a non-existent optionId")
    fun `should throw exception for non-existent optionId`() {
        val nonExistentOptionId = 999L
        val request =
            PlaceOrderRequest(
                orderItems = listOf(OrderItemRequest(optionId = nonExistentOptionId, quantity = 1)),
                PaymentRequest(9000, "EUR", "pm_card_visa"),
            )

        assertThrows<IllegalStateException> {
            orderCreationService.createPending(member.id, request)
        }
    }
}
