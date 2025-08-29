package ecommerce.service

import ecommerce.entity.Member
import ecommerce.entity.Order
import ecommerce.entity.PaymentAttempt
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
class PaymentAttemptServiceIntegrationTest {
    @Autowired
    private lateinit var paymentAttemptService: PaymentAttemptService

    @Autowired
    private lateinit var entityManager: TestEntityManager

    private lateinit var existingPaymentAttempt: PaymentAttempt

    @BeforeEach
    fun setup() {
        val member = Member(email = "payment-user@test.com", password = "pw")
        entityManager.persist(member)

        val order = Order(member)
        entityManager.persist(order)

        existingPaymentAttempt =
            PaymentAttempt(
                order = order,
                status = PaymentAttemptStatus.PENDING,
            )
        entityManager.persistAndFlush(existingPaymentAttempt)
    }

    @Nested
    @DisplayName("Tets for function updateStatus")
    inner class UpdateStatusTests {
        @Test
        fun `successfully updates the status of an existing payment attempt`() {
            val newStatus = PaymentAttemptStatus.APPROVED

            paymentAttemptService.updateStatus(existingPaymentAttempt.id, newStatus)
            entityManager.flush()
            entityManager.clear()

            val updatedAttempt = entityManager.find(PaymentAttempt::class.java, existingPaymentAttempt.id)
            assertThat(updatedAttempt.status).isEqualTo(newStatus)
        }

        @Test
        fun `should throw an exception for a non-existent payment attempt update`() {
            val nonExistentId = 999L

            assertThrows<NoSuchElementException> {
                paymentAttemptService.updateStatus(nonExistentId, PaymentAttemptStatus.REJECTED)
            }
        }
    }

    @Nested
    @DisplayName("Test for function findById")
    inner class FindByIdTests {
        @Test
        fun `should return an existing payment attempt`() {
            val foundAttempt = paymentAttemptService.findById(existingPaymentAttempt.id)

            assertThat(foundAttempt).isNotNull()
            assertThat(foundAttempt.id).isEqualTo(existingPaymentAttempt.id)
            assertThat(foundAttempt.status).isEqualTo(PaymentAttemptStatus.PENDING)
        }

        @Test
        fun `should throw an exception for a non-existent payment attempt`() {
            val nonExistentId = 999L

            assertThrows<NoSuchElementException> {
                paymentAttemptService.findById(nonExistentId)
            }
        }
    }
}
