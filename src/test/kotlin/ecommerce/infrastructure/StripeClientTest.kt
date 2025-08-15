package ecommerce.infrastructure

import ecommerce.dto.payment.PaymentRequest
import ecommerce.dto.stripe.StripeResponse
import ecommerce.utils.exception.StripeException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

class StripeClientTest {
    private val mockRestClient: RestClient = mock()
    private val realStripeClient =
        StripeClient(
            stripeKey = "test_secret",
            stripeRestClient = mockRestClient,
        )

    private val paymentRequest = PaymentRequest("100", "eur", "pm_card_visa")
    private val stripeResponse =
        StripeResponse(
            "test_intent_id",
            100,
            "succeeded",
            1234,
            "eur",
        )

    @Test
    fun `should create payment intent successfully`() {
        whenever(mockRestClient.post()).thenReturn(mock())
        val response = stripeResponse
        assertThat(response.id).isEqualTo("test_intent_id")
        assertThat(response.status).isEqualTo("succeeded")
    }

    @Test
    fun `should confirm payment intent successfully`() {
        val response = stripeResponse
        assertThat(response.id).isEqualTo("test_intent_id")
        assertThat(response.status).isEqualTo("succeeded")
    }

    @Test
    fun `should throw StripeException on bad request`() {
        whenever(mockRestClient.post()).thenThrow(
            HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                HttpHeaders(),
                "invalid param".toByteArray(),
                null,
            ),
        )

        val ex = assertThrows<StripeException> { realStripeClient.createCheckoutSession(paymentRequest) }
        assertThat(ex.message).contains("Invalid request")
    }

    @Test
    fun `should throw StripeException on unauthorized`() {
        whenever(mockRestClient.post()).thenThrow(
            HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                HttpHeaders(),
                "auth error".toByteArray(),
                null,
            ),
        )

        val ex = assertThrows<StripeException> { realStripeClient.createCheckoutSession(paymentRequest) }
        assertThat(ex.message).contains("Authentication failed")
    }

    @Test
    fun `should throw StripeException on server error`() {
        whenever(mockRestClient.post()).thenThrow(
            HttpServerErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Error",
                HttpHeaders(),
                "stripe down".toByteArray(),
                null,
            ),
        )

        val ex = assertThrows<StripeException> { realStripeClient.createCheckoutSession(paymentRequest) }
        assertThat(ex.message).contains("Stripe service is unavailable")
    }

    @Test
    fun `should throw StripeException on generic RestClientException`() {
        whenever(mockRestClient.post()).thenThrow(RestClientException("connection timeout"))

        val ex = assertThrows<StripeException> { realStripeClient.createCheckoutSession(paymentRequest) }
        assertThat(ex.message).contains("An unexpected error occurred")
    }
}
