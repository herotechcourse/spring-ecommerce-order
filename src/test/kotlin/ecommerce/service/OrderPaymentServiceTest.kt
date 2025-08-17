package ecommerce.service

import ecommerce.client.PaymentRequest
import ecommerce.client.PaymentResponse
import ecommerce.client.StripeClient
import ecommerce.exception.PaymentFailedException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class OrderPaymentServiceTest {
    @Mock
    lateinit var stripeClient: StripeClient

    lateinit var orderPaymentService: OrderPaymentService

    @BeforeEach
    fun setUp() {
        orderPaymentService = OrderPaymentService(stripeClient)
    }

    @Test
    fun `payment status success with valid payment method`() {
        // Given
        val amount = 100
        val currency = "USD"
        val paymentMethod = "pm_card_visa"

        val request =
            PaymentRequest(
                amount = amount,
                currency = currency,
                paymentMethod = paymentMethod,
            )

        val mockResponse =
            PaymentResponse(
                id = "pi_abc",
                `object` = "payment_intent",
                amount = amount,
                payment_method = "pm_abc",
                client_secret = "pi_secret",
                confirmation_method = "automatic",
                status = "succeeded",
                next_action = null,
            )

        whenever(stripeClient.createCheckoutSession(request)).thenReturn(mockResponse)

        // When
        val actual = orderPaymentService.initiatePayment(request)

        // Then
        assertThat(actual).isNotNull()
        assertThat(actual.amount).isEqualTo(amount)
        assertThat(actual.`object`).isEqualTo("payment_intent")
        assertThat(actual.status).isEqualTo("succeeded")
    }

    @Test
    fun `payment return error with invalid payment method`() {
        // Given
        val amount = 100
        val currency = "USD"
        val paymentMethod = "pm_card_visa_chargeDeclined"

        val request =
            PaymentRequest(
                amount = amount,
                currency = currency,
                paymentMethod = paymentMethod,
            )

        whenever(stripeClient.createCheckoutSession(request)).thenThrow(PaymentFailedException::class.java)

        // When
        // Then
        assertThrows<PaymentFailedException> { orderPaymentService.initiatePayment(request) }
    }
}
