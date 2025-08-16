package ecommerce.stripe

import ecommerce.dto.order.PlaceOrderRequestDto
import ecommerce.dto.stripe.PaymentResponseDto
import ecommerce.enums.OrderStatus
import ecommerce.exception.PaymentException
import ecommerce.exception.StripePaymentException
import ecommerce.model.Cart
import ecommerce.model.CartProduct
import ecommerce.model.Option
import ecommerce.model.User
import ecommerce.repository.CartProductRepository
import ecommerce.repository.OptionRepository
import ecommerce.repository.OrderRepository
import ecommerce.repository.UserRepository
import ecommerce.service.OrderService
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@Import(OrderServiceFailureTest.TestStripeConfig::class)
class OrderServiceFailureTest {
    @Autowired lateinit var orderService: OrderService

    @Autowired lateinit var orderRepository: OrderRepository

    @Autowired lateinit var userRepository: UserRepository

    @Autowired lateinit var optionRepository: OptionRepository

    @Autowired lateinit var cartProductRepository: CartProductRepository

    @Autowired lateinit var entityManager: EntityManager

    @Autowired lateinit var stripeClient: StripeClient

    private lateinit var user: User
    private lateinit var option: Option
    private val initialStock = 10

    @BeforeEach
    fun setUp() {
        val cart = Cart()
        entityManager.persist(cart)

        user =
            User(
                "u${System.nanoTime()}@example.com",
                "secret123",
                "Test User",
            ).also { it.cart = cart }
        user = userRepository.save(user)

        option =
            Option(
                "Option A",
                5.0,
                initialStock,
                "https://example.com/p.png",
            )
        option = optionRepository.save(option)

        val cartProduct = CartProduct(cart = cart, option = option, quantity = 2)
        cartProductRepository.save(cartProduct)

        entityManager.flush()
        entityManager.clear()
    }

    @Test
    @Transactional
    fun `payment returns non-succeeded - order FAILED with session id, reason mapped, stock unchanged`() {
        whenever(stripeClient.createCheckoutSession(any()))
            .thenReturn(
                PaymentResponseDto(
                    "sess_req_action",
                    10_00L,
                    "EUR",
                    "requires_action",
                    null,
                ),
            )

        val req = PlaceOrderRequestDto(currency = "EUR", paymentMethodId = "pm_xxx")

        val ex =
            assertThrows(PaymentException::class.java) {
                orderService.placeOrder(user.id, req)
            }
        assertTrue(
            ex.message!!.contains("Additional authentication is required"),
            "Expected friendly message for requires_action",
        )

        val orders = orderRepository.findAllByUserOrderByCreatedAtDesc(user)
        assertTrue(orders.isNotEmpty())
        val order = orders.first()

        assertEquals(OrderStatus.FAILED, order.status)
        assertNotNull(order.failureReason)
        assertEquals("sess_req_action", order.stripeSessionId)

        val freshOpt = optionRepository.findById(option.id).orElseThrow()
        assertEquals(initialStock, freshOpt.quantity, "stock must remain unchanged on failure")
    }

    @Test
    @Transactional
    fun `stripe throws exception - order FAILED without session id, stock unchanged`() {
        whenever(stripeClient.createCheckoutSession(any()))
            .thenThrow(
                StripePaymentException(
                    "Stripe rejected the payment",
                    code = "insufficient_funds",
                    declineCode = null,
                ),
            )

        val req = PlaceOrderRequestDto(currency = "EUR", paymentMethodId = "pm_zzz")

        val ex =
            assertThrows(PaymentException::class.java) {
                orderService.placeOrder(user.id, req)
            }
        assertTrue(
            ex.message!!.contains("insufficient"),
            "Expected friendly message for insufficient_funds",
        )

        val order = orderRepository.findAllByUserOrderByCreatedAtDesc(user).first()
        assertEquals(OrderStatus.FAILED, order.status)
        assertNotNull(order.failureReason)
        assertTrue(order.stripeSessionId.isEmpty(), "session id should be empty when session creation failed")

        val freshOpt = optionRepository.findById(option.id).orElseThrow()
        assertEquals(initialStock, freshOpt.quantity, "stock must remain unchanged on failure")
    }

    @TestConfiguration
    class TestStripeConfig {
        @Bean
        @Primary
        fun stripeClient(): StripeClient = mock()
    }
}
