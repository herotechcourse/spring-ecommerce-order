package ecommerce.service

import ecommerce.dto.PaymentResponse
import ecommerce.entity.Member
import ecommerce.entity.Option
import ecommerce.entity.Order
import ecommerce.entity.OrderItem
import ecommerce.entity.PaymentAttempt
import ecommerce.entity.Product
import ecommerce.entity.enumerated.OrderStatus
import ecommerce.entity.enumerated.PaymentAttemptStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
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
class PaymentFinalizationServiceIntegrationTest {
    @Autowired
    private lateinit var paymentFinalizationService: PaymentFinalizationService

    @Autowired
    private lateinit var entityManager: TestEntityManager

    private lateinit var order: Order
    private lateinit var paymentAttempt: PaymentAttempt
    private lateinit var option: Option
    private lateinit var product: Product
    private val initialStock = 10
    private val orderedQuantity = 3

    @BeforeEach
    fun setup() {
        val member = Member(email = "finalizer@test.com", password = "pw")
        entityManager.persist(member)

        option = Option(name = "Wool", quantity = initialStock)
        product = Product(name = "Scarf", price = 1500, "http://test.png", listOf(option))
        product.options.first().quantity -= orderedQuantity
        entityManager.persist(product)

        order = Order(member, status = OrderStatus.PENDING)
        val orderItem =
            OrderItem(
                order = order,
                productId = product.id,
                optionId = option.id,
                productNameSnapshot = product.name,
                priceSnapshot = product.price,
                quantity = orderedQuantity,
            )
        order.items.add(orderItem)
        order.totalAmount = product.price * orderedQuantity

        entityManager.persist(order)

        paymentAttempt = PaymentAttempt(order = order, status = PaymentAttemptStatus.PENDING)
        entityManager.persist(paymentAttempt)

        entityManager.flush()
    }

    @Nested
    @DisplayName("finalizePaid() tests")
    inner class FinalizePaidTests {
        @Test
        fun `should update order and payment statuses on successful payment`() {
            val response = paymentFinalizationService.finalizePaid(order.id, paymentAttempt.id)

            entityManager.flush()
            entityManager.clear()

            val updatedOrder = entityManager.find(Order::class.java, order.id)
            val updatedPaymentAttempt = entityManager.find(PaymentAttempt::class.java, paymentAttempt.id)

            assertThat(updatedOrder.status).isEqualTo(OrderStatus.PAID)
            assertThat(updatedPaymentAttempt.status).isEqualTo(PaymentAttemptStatus.APPROVED)

            assertThat(response.orderId).isEqualTo(order.id)
            assertThat(response.status).isEqualTo(OrderStatus.PAID)
            assertThat(response.totalAmount).isEqualTo(order.totalAmount)
        }

        @Test
        fun `should throw exception when orderId does not exist`() {
            val nonExistentOrderId = 999L

            assertThrows<NoSuchElementException> {
                paymentFinalizationService.finalizePaid(nonExistentOrderId, paymentAttempt.id)
            }
        }
    }

    @Nested
    @DisplayName("finalizeFailed() tests")
    inner class FinalizeFailedTests {
        @Test
        fun `should update statuses and restore item stock on failed payment`() {
            val paymentResponse = PaymentResponse(id = "payment_123", amount = 4500)

            val response = paymentFinalizationService.finalizeFailed(order.id, paymentAttempt.id, paymentResponse)

            entityManager.flush()
            entityManager.clear()

            val updatedOrder = entityManager.find(Order::class.java, order.id)
            val updatedPaymentAttempt = entityManager.find(PaymentAttempt::class.java, paymentAttempt.id)

            assertThat(updatedOrder.status).isEqualTo(OrderStatus.FAILED)
            assertThat(updatedPaymentAttempt.status).isEqualTo(PaymentAttemptStatus.REJECTED)

            val updatedOption = entityManager.find(Option::class.java, option.id)
            assertThat(updatedOption.quantity).isEqualTo(initialStock)

            assertThat(response.orderId).isEqualTo(order.id)
            assertThat(response.status).isEqualTo(OrderStatus.FAILED)
            assertThat(response.paymentId).isEqualTo(paymentResponse.id)
        }

        @Test
        fun `should throw exception when paymentAttemptId does not exist`() {
            val nonExistentAttemptId = 999L

            assertThrows<NoSuchElementException> {
                paymentFinalizationService.finalizeFailed(order.id, nonExistentAttemptId, null)
            }
        }
    }
}
