package ecommerce.service

import ecommerce.config.StripeClient
import ecommerce.dto.checkout.CheckoutResponse
import ecommerce.dto.payment.PaymentIntentRequest
import ecommerce.exception.FailedPaymentException
import ecommerce.model.Currency
import ecommerce.model.Member
import ecommerce.model.Order
import ecommerce.model.OrderItem
import ecommerce.model.OrderStatus
import ecommerce.model.Product
import ecommerce.model.ProductOption
import ecommerce.model.Role
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class PaymentServiceTest {
    @Mock
    private lateinit var stripeClient: StripeClient

    private lateinit var paymentService: PaymentService

    private lateinit var order: Order
    private lateinit var paymentIntentRequest: PaymentIntentRequest

    @BeforeEach
    fun setUp() {
        paymentService = PaymentService(stripeClient)

        val member = Member("test@test.com", "password", "Test User", Role.USER, id = 1L)
        val product = Product("Test Product", 29.99, 10, "test.jpg", id = 1L)
        val productOption = ProductOption("Size M", 10, product, 29.99, id = 1L)

        order = Order(member, currency = Currency.EUR, id = 1L)
        val orderItem = OrderItem.fromProductOption(productOption, 2, order)
        order.addOrderItem(orderItem)

        paymentIntentRequest =
            PaymentIntentRequest(
                amount = 59.98,
                currency = Currency.EUR,
                paymentMethod = "pm_card_visa",
            )
    }

    @Test
    fun `should process payment successfully`() {
        // Given
        val stripeResponse =
            CheckoutResponse(
                id = "cs_test_123",
                client_secret = "cs_test_123_secret",
                amount = 5998,
                currency = "eur",
                status = "open",
                payment_method = null,
                orderId = 1L,
                orderStatus = "PENDING",
                items = emptyList(),
            )

        `when`(stripeClient.createCheckoutSession(paymentIntentRequest)).thenReturn(stripeResponse)

        // When
        val result = paymentService.processPayment(paymentIntentRequest, order)

        // Then
        assertThat(result.id).isEqualTo("cs_test_123")
        assertThat(result.orderId).isEqualTo(1L)
        assertThat(result.orderStatus).isEqualTo("PENDING")
        assertThat(result.amount).isEqualTo(5998) // Stripe expects cents
        assertThat(result.currency).isEqualTo("eur")
        assertThat(result.items.size).isEqualTo(1)
        assertThat(order.stripeCheckoutSessionId).isEqualTo("cs_test_123")
    }

    @Test
    fun `should throw FailedPaymentException when Stripe client returns null`() {
        // Given
        `when`(stripeClient.createCheckoutSession(paymentIntentRequest)).thenReturn(null)

        // When & Then
        assertThrows(FailedPaymentException::class.java) {
            paymentService.processPayment(paymentIntentRequest, order)
        }
    }

    @Test
    fun `should update checkout response with correct order data`() {
        // Given
        val stripeResponse =
            CheckoutResponse(
                id = "cs_test_123",
                client_secret = null,
                amount = 0,
                currency = "usd",
                status = "open",
                payment_method = null,
                orderId = 0L,
                orderStatus = "CONFIRMED",
                items = emptyList(),
            )

        `when`(stripeClient.createCheckoutSession(paymentIntentRequest)).thenReturn(stripeResponse)

        // When
        val result = paymentService.processPayment(paymentIntentRequest, order)

        // Then
        assertThat(result.orderId).isEqualTo(1L)
        assertThat(result.orderStatus).isEqualTo(OrderStatus.PENDING.name)
        assertThat(result.amount).isEqualTo((order.totalAmount * 100).toInt())
        assertThat(result.currency).isEqualTo(Currency.EUR.name.lowercase())
        assertThat(result.items.size).isEqualTo(1)
    }
}
