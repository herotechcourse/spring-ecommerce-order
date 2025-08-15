package ecommerce.integration

import ecommerce.exception.PaymentFailedException
import ecommerce.infrastructure.StripeClient
import ecommerce.model.PaymentRequest
import ecommerce.model.StripeResponse
import ecommerce.services.stripe.StripeService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.springframework.util.LinkedMultiValueMap
import java.net.SocketTimeoutException
import kotlin.test.assertEquals

class StripeServiceTest {
    private lateinit var stripeClient: StripeClient
    private lateinit var stripeService: StripeService

    @BeforeEach
    fun setUp() {
        stripeClient = mockk()
        stripeService = StripeService(stripeClient)
    }

    @Test
    fun `createPaymentIntent successfully creates payment intent with correct parameters`() {
        // Given
        val paymentRequest =
            PaymentRequest(
                amount = 1000.0,
                currency = "usd",
                paymentMethod = "pm_card_visa",
            )

        val expectedForm =
            LinkedMultiValueMap<String, String>().apply {
                add("amount", "100000") // 1000.0 * 100
                add("currency", "usd")
                add("payment_method", "pm_card_visa")
                add("confirm", "true")
                add("automatic_payment_methods[enabled]", "true")
                add("automatic_payment_methods[allow_redirects]", "never")
            }

        val mockResponse =
            StripeResponse(
                id = "pi_test123",
                amount = 100000,
                currency = "usd",
                status = "succeeded",
                clientSecret = "secret_test123",
                cancellationReason = "",
                customer = "",
                failureCode = "",
                failureMessage = "",
                created = "",
                paymentMethod = "",
                latestCharge = "",
            )

        every { stripeClient.createPaymentIntent(expectedForm) } returns mockResponse

        // When
        val response = stripeService.createPaymentIntent(paymentRequest)

        // Then
        assertNotNull(response)
        assertEquals("pi_test123", response.id)
        assertEquals(100000, response.amount)
        assertEquals("usd", response.currency)
        assertEquals("succeeded", response.status)

        verify(exactly = 1) { stripeClient.createPaymentIntent(expectedForm) }
    }

    @Test
    fun `createPaymentIntent with confirm false passes correct parameter`() {
        // Given
        val paymentRequest =
            PaymentRequest(
                amount = 500.0,
                currency = "eur",
                paymentMethod = "pm_card_mastercard",
            )

        val expectedForm =
            LinkedMultiValueMap<String, String>().apply {
                add("amount", "50000") // 500.0 * 100
                add("currency", "eur")
                add("payment_method", "pm_card_mastercard")
                add("confirm", "false")
                add("automatic_payment_methods[enabled]", "true")
                add("automatic_payment_methods[allow_redirects]", "never")
            }

        val mockResponse =
            StripeResponse(
                id = "pi_test456",
                amount = 50000,
                currency = "eur",
                status = "requires_payment_method",
                clientSecret = "secret_test456",
                cancellationReason = "",
                customer = "",
                failureCode = "",
                failureMessage = "",
                created = "",
                paymentMethod = "",
                latestCharge = "",
            )

        every { stripeClient.createPaymentIntent(expectedForm) } returns mockResponse

        // When
        val response = stripeService.createPaymentIntent(paymentRequest, isConfirmed = false)

        // Then
        assertEquals("requires_payment_method", response.status)
        verify(exactly = 1) { stripeClient.createPaymentIntent(expectedForm) }
    }

    @Test
    fun `createPaymentIntent correctly converts amount to smallest unit`() {
        // Given
        val paymentRequest =
            PaymentRequest(
                amount = 99.99,
                currency = "usd",
                paymentMethod = "pm_card_visa",
            )

        val expectedForm =
            LinkedMultiValueMap<String, String>().apply {
                add("amount", "9999") // 99.99 * 100
                add("currency", "usd")
                add("payment_method", "pm_card_visa")
                add("confirm", "true")
                add("automatic_payment_methods[enabled]", "true")
                add("automatic_payment_methods[allow_redirects]", "never")
            }

        val mockResponse =
            StripeResponse(
                id = "pi_test789",
                amount = 9999,
                currency = "usd",
                status = "succeeded",
                clientSecret = "secret_test789",
                cancellationReason = "",
                customer = "",
                failureCode = "",
                failureMessage = "",
                created = "",
                paymentMethod = "",
                latestCharge = "",
            )

        every { stripeClient.createPaymentIntent(expectedForm) } returns mockResponse

        // When
        val response = stripeService.createPaymentIntent(paymentRequest)

        // Then
        assertEquals(9999, response.amount)
        verify(exactly = 1) { stripeClient.createPaymentIntent(expectedForm) }
    }

    @Test
    fun `createPaymentIntent throws PaymentFailedException when Stripe client fails`() {
        // Given
        val paymentRequest =
            PaymentRequest(
                amount = 1000.0,
                currency = "usd",
                paymentMethod = "pm_card_visa",
            )

        val stripeError = RuntimeException("Card declined")
        every { stripeClient.createPaymentIntent(any()) } throws stripeError

        // When & Then
        val exception =
            assertThrows<PaymentFailedException> {
                stripeService.createPaymentIntent(paymentRequest)
            }

        assertTrue(exception.message!!.contains("Stripe payment initialization failed"))
        assertTrue(exception.message!!.contains("Card declined"))
        assertEquals(stripeError, exception.cause)

        verify(exactly = 1) { stripeClient.createPaymentIntent(any()) }
    }

    @Test
    fun `createPaymentIntent handles network timeout exception`() {
        // Given
        val paymentRequest =
            PaymentRequest(
                amount = 2000.0,
                currency = "gbp",
                paymentMethod = "pm_card_amex",
            )

        val timeoutError = SocketTimeoutException("Connection timeout")
        every { stripeClient.createPaymentIntent(any()) } throws timeoutError

        // When & Then
        val exception =
            assertThrows<PaymentFailedException> {
                stripeService.createPaymentIntent(paymentRequest)
            }

        assertTrue(exception.message!!.contains("Stripe payment initialization failed"))
        assertTrue(exception.message!!.contains("Connection timeout"))
        assertEquals(timeoutError, exception.cause)
    }

    @Test
    fun `createPaymentIntent handles invalid API key exception`() {
        // Given
        val paymentRequest =
            PaymentRequest(
                amount = 100.0,
                currency = "usd",
                paymentMethod = "pm_card_visa",
            )

        val apiKeyError = IllegalArgumentException("Invalid API key provided")
        every { stripeClient.createPaymentIntent(any()) } throws apiKeyError

        // When & Then
        val exception =
            assertThrows<PaymentFailedException> {
                stripeService.createPaymentIntent(paymentRequest)
            }

        assertTrue(exception.message!!.contains("Stripe payment initialization failed"))
        assertTrue(exception.message!!.contains("Invalid API key"))
        assertEquals(apiKeyError, exception.cause)
    }
}
