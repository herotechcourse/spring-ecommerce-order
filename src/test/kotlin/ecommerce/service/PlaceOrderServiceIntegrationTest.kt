package ecommerce.service

import ecommerce.dto.OrderItemRequest
import ecommerce.dto.PaymentRequest
import ecommerce.dto.PaymentResponse
import ecommerce.dto.PlaceOrderRequest
import ecommerce.entity.Cart
import ecommerce.entity.CartItem
import ecommerce.entity.Member
import ecommerce.entity.Option
import ecommerce.entity.Product
import ecommerce.entity.enumerated.OrderStatus
import ecommerce.entity.enumerated.PaymentAttemptStatus
import ecommerce.repository.CartJpaRepository
import ecommerce.repository.OrderJpaRepository
import ecommerce.repository.PaymentAttemptJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@AutoConfigureTestEntityManager
class PlaceOrderServiceIntegrationTest {
    @Autowired
    private lateinit var placeOrderService: PlaceOrderService

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var orderRepo: OrderJpaRepository

    @Autowired
    private lateinit var paymentAttemptRepo: PaymentAttemptJpaRepository

    @Autowired
    private lateinit var cartRepo: CartJpaRepository

    @MockitoBean
    private lateinit var paymentService: PaymentService

    private lateinit var member: Member
    private lateinit var option: Option
    private lateinit var placeOrderRequest: PlaceOrderRequest
    private val initialStock = 10
    private val orderedQuantity = 2

    @BeforeEach
    fun setup() {
        member = Member(email = "placer@test.com", password = "pw")
        entityManager.persist(member)

        option = Option(name = "Hardcover", quantity = initialStock)
        val product = Product(name = "Book", price = 1500, "http://test.png", listOf(option))
        entityManager.persist(product)

        val cart = Cart(member = member)
        cart.items.add(CartItem(cart, option, orderedQuantity))
        entityManager.persist(cart)

        entityManager.flush()

        placeOrderRequest =
            PlaceOrderRequest(
                orderItems = listOf(OrderItemRequest(optionId = option.id, quantity = orderedQuantity)),
                paymentRequest = PaymentRequest(amount = 3000, currency = "EUR", paymentMethod = "PAYMENT_METHOD"),
            )
    }

    @Nested
    @DisplayName("when payment is successful")
    inner class SuccessfulPaymentTests {
        @Test
        fun `should finalize order as PAID, decrease stock, and clear the cart`() {
            val paymentResponse = PaymentResponse(id = "payment_success_123", amount = 3000)
            whenever(paymentService.pay(any())).thenReturn(paymentResponse)

            val response = placeOrderService.placeOrder(member.id, placeOrderRequest)

            entityManager.flush()
            entityManager.clear()

            val order = orderRepo.findAll().first()
            val attempt = paymentAttemptRepo.findAll().first()
            assertThat(order.status).isEqualTo(OrderStatus.PAID)
            assertThat(attempt.status).isEqualTo(PaymentAttemptStatus.APPROVED)

            val updatedOption = entityManager.find(Option::class.java, option.id)
            assertThat(updatedOption.quantity).isEqualTo(initialStock - orderedQuantity)

            val cart = cartRepo.findByMemberId(member.id)
            assertThat(cart?.items).isEmpty()

            assertThat(response.status).isEqualTo(OrderStatus.PAID)
        }
    }

    @Nested
    @DisplayName("when payment fails")
    inner class FailedPaymentTests {
        @Test
        fun `should finalize order as FAILED, restore stock, and not clear the cart`() {
            whenever(paymentService.pay(any())).thenReturn(null)

            val response = placeOrderService.placeOrder(member.id, placeOrderRequest)

            entityManager.flush()
            entityManager.clear()

            val order = orderRepo.findAll().first()
            val attempt = paymentAttemptRepo.findAll().first()
            assertThat(order.status).isEqualTo(OrderStatus.FAILED)
            assertThat(attempt.status).isEqualTo(PaymentAttemptStatus.REJECTED)

            val updatedOption = entityManager.find(Option::class.java, option.id)
            assertThat(updatedOption.quantity).isEqualTo(initialStock)

            val cart = cartRepo.findByMemberId(member.id)
            assertThat(cart?.items).isNotEmpty()

            assertThat(response.status).isEqualTo(OrderStatus.FAILED)
        }
    }
}
