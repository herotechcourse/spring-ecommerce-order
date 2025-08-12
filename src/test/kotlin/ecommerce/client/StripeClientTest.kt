import ecommerce.client.StripeClient
import ecommerce.config.StripeProperties
import ecommerce.dto.PaymentRequest
import ecommerce.exception.BadRequestException
import ecommerce.exception.ExternalServiceException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("UNCHECKED_CAST")
class StripeClientTest {
    @Mock
    private val stripeProperties = StripeProperties(secretKey = "sk_test_123")

    private val restClient: RestClient = mock(RestClient::class.java)

    private val stripeClient = StripeClient(stripeProperties, restClient)

    @Test
    fun `should return body when Stripe responds successfully`() {
        val req = PaymentRequest(1000, "usd", "card")

        val postSpec = mock(RestClient.RequestBodyUriSpec::class.java)
        val retrieveSpec = mock(RestClient.ResponseSpec::class.java)
        val stripeJsonResponse =
            """
            {
              "id": "pi_3Ruvv61KGHaCa7kl1sC7t88M",
              "object": "payment_intent",
              "amount": 720
            }
            """.trimIndent()

        `when`(restClient.post()).thenReturn(postSpec)
        `when`(postSpec.uri(anyString())).thenReturn(postSpec)
        `when`(postSpec.header(anyString(), anyString())).thenReturn(postSpec)
        `when`(postSpec.contentType(any())).thenReturn(postSpec)
        `when`(postSpec.body(any<String>())).thenReturn(postSpec)
        `when`(postSpec.retrieve()).thenReturn(retrieveSpec)
        `when`(retrieveSpec.onStatus(any(), any())).thenReturn(retrieveSpec)
        `when`(retrieveSpec.toEntity(String::class.java)).thenReturn(ResponseEntity.ok(stripeJsonResponse))

        val result = stripeClient.createCheckoutSession(req).id

        val bodyCaptor = argumentCaptor<String>()
        verify(postSpec).body(bodyCaptor.capture())

        val capturedBody = bodyCaptor.firstValue

        assertTrue(capturedBody.contains("amount=1000"))
        assertTrue(capturedBody.contains("currency=usd"))
        assertTrue(capturedBody.contains("payment_method=card"))

        assertEquals("pi_3Ruvv61KGHaCa7kl1sC7t88M", result)
    }

    @Test
    fun `should throw BadRequestException when CardException occurs`() {
        val req = PaymentRequest(1000, "usd", "card")

        val postSpec = mock(RestClient.RequestBodyUriSpec::class.java)
        val retrieveSpec = mock(RestClient.ResponseSpec::class.java)
        // Simulate Stripe returning an HTTP 500 error
        val stripeErrorJson =
            """
            {
              "error": {
                "message": "Card declined",
                "type": "api_error",
                "code": "internal_error"
              }
            }
            """.trimIndent()
        val stripeError =
            RestClientResponseException(
                "Bad Request",
                400,
                "Bad Request",
                null,
                stripeErrorJson.toByteArray(),
                null,
            )
        `when`(restClient.post()).thenReturn(postSpec)
        `when`(postSpec.uri(anyString())).thenReturn(postSpec)
        `when`(postSpec.header(anyString(), anyString())).thenReturn(postSpec)
        `when`(postSpec.contentType(any())).thenReturn(postSpec)
        `when`(postSpec.body(any<String>())).thenReturn(postSpec)
        `when`(postSpec.retrieve()).thenReturn(retrieveSpec)
        `when`(retrieveSpec.toEntity(String::class.java)).thenThrow(stripeError)

        val ex =
            assertThrows<BadRequestException> {
                stripeClient.createCheckoutSession(req)
            }
        assertThat(ex.message!!.contains("Card declined")).isTrue()
    }

    @Test
    fun `should throw RuntimeException when generic StripeException occurs`() {
        val req = PaymentRequest(1000, "usd", "card")

        val postSpec = mock(RestClient.RequestBodyUriSpec::class.java)
        val retrieveSpec = mock(RestClient.ResponseSpec::class.java)
        // Simulate Stripe returning an HTTP 500 error
        val stripeErrorJson =
            """
            {
              "error": {
                "message": "Stripe is down",
                "type": "api_error",
                "code": "internal_error"
              }
            }
            """.trimIndent()
        val stripeError =
            RestClientResponseException(
                "Internal Server Error",
                500,
                "Internal Server Error",
                null,
                stripeErrorJson.toByteArray(),
                null,
            )

        `when`(restClient.post()).thenReturn(postSpec)
        `when`(postSpec.uri(anyString())).thenReturn(postSpec)
        `when`(postSpec.header(anyString(), anyString())).thenReturn(postSpec)
        `when`(postSpec.contentType(any())).thenReturn(postSpec)
        `when`(postSpec.body(any<String>())).thenReturn(postSpec)
        `when`(postSpec.retrieve()).thenReturn(retrieveSpec)
        `when`(retrieveSpec.toEntity(String::class.java)).thenThrow(stripeError)

        assertThrows<ExternalServiceException> {
            stripeClient.createCheckoutSession(req)
        }
    }
}
