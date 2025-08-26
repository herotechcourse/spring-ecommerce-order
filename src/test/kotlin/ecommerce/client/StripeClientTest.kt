package ecommerce.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ecommerce.config.StripeProperties
import ecommerce.dto.payment.StripeErrorDetails
import ecommerce.dto.payment.StripeErrorResponse
import ecommerce.dto.payment.StripePaymentResponse
import ecommerce.exception.PaymentClientException
import ecommerce.exception.PaymentServerException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClient.RequestBodySpec
import org.springframework.web.client.RestClient.RequestBodyUriSpec
import org.springframework.web.client.RestClient.ResponseSpec

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension::class)
class StripeClientTest {
    private val stripeProps = StripeProperties(secretKey = "sk_test_invalid")
    private val objectMapper = jacksonObjectMapper()
    private val restClient: RestClient = mock()
    private val uriSpec: RequestBodyUriSpec = mock()
    private val bodySpec: RequestBodySpec = mock()
    private val responseSpec: ResponseSpec = mock()

    private fun buildClient(): StripeClient =
        StripeClient(
            stripeProperties = stripeProps,
            objectMapper = objectMapper,
            restClient = restClient,
        )

    private fun stubCommonChain() {
        whenever(restClient.post()).thenReturn(uriSpec)
        whenever(uriSpec.uri(any<String>())).thenReturn(bodySpec)
        whenever(bodySpec.header(any<String>(), any<String>())).thenReturn(bodySpec)
        whenever(bodySpec.contentType(any<MediaType>())).thenReturn(bodySpec)
        whenever(bodySpec.body(any<LinkedMultiValueMap<String, String>>())).thenReturn(bodySpec)
        whenever(bodySpec.retrieve()).thenReturn(responseSpec)
    }

    @Test
    fun `success maps to StripePaymentResponse`() {
        stubCommonChain()
        val fakeResponse =
            StripePaymentResponse(
                id = "pi_123",
                status = "succeeded",
                amount = 1000L,
                currency = "usd",
                lastPaymentError = null,
            )
        whenever(responseSpec.body(StripePaymentResponse::class.java)).thenReturn(fakeResponse)

        val client = buildClient()
        val result = client.createPaymentIntent(amount = 1000, currency = "usd")

        assertThat(result).isEqualTo(fakeResponse)
    }

    @Test
    fun `4xx error maps to PaymentClientException with Stripe message`() {
        stubCommonChain()
        val stripeErrorJson =
            objectMapper.writeValueAsString(
                StripeErrorResponse(error = StripeErrorDetails(message = "Invalid API Key", code = "invalid_key")),
            )
        val httpEx =
            HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                HttpHeaders().apply { add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) },
                stripeErrorJson.toByteArray(),
                null,
            )
        whenever(responseSpec.body(StripePaymentResponse::class.java)).thenThrow(httpEx)

        val client = buildClient()

        assertThatThrownBy { client.createPaymentIntent(1000, "usd") }
            .isInstanceOf(PaymentClientException::class.java)
            .hasMessageContaining("Invalid API Key")
    }

    @Test
    fun `5xx error maps to PaymentServerException`() {
        stubCommonChain()
        val httpEx =
            HttpServerErrorException.create(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Service Unavailable",
                HttpHeaders.EMPTY,
                byteArrayOf(),
                null,
            )
        whenever(responseSpec.body(StripePaymentResponse::class.java)).thenThrow(httpEx)

        val client = buildClient()

        assertThatThrownBy { client.createPaymentIntent(1000, "usd") }
            .isInstanceOf(PaymentServerException::class.java)
    }

    @Test
    fun `IO timeout maps to PaymentServerException`() {
        stubCommonChain()
        whenever(responseSpec.body(StripePaymentResponse::class.java)).thenThrow(ResourceAccessException("timeout"))

        val client = buildClient()

        assertThatThrownBy { client.createPaymentIntent(1000, "usd") }
            .isInstanceOf(PaymentServerException::class.java)
            .hasMessageContaining("Could not connect to payment service")
    }
}
