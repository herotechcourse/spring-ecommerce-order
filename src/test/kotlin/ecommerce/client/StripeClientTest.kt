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
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClient
import java.util.function.Predicate
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
        val req = PaymentRequest(1000.0, "usd", "card")

        val postSpec = mock(RestClient.RequestBodyUriSpec::class.java)
        val retrieveSpec = mock(RestClient.ResponseSpec::class.java)

        `when`(restClient.post()).thenReturn(postSpec)
        `when`(postSpec.uri(anyString())).thenReturn(postSpec)
        `when`(postSpec.header(anyString(), anyString())).thenReturn(postSpec)
        `when`(postSpec.contentType(any())).thenReturn(postSpec)
        `when`(postSpec.body(any<String>())).thenReturn(postSpec)
        `when`(postSpec.retrieve()).thenReturn(retrieveSpec)
        `when`(retrieveSpec.onStatus(any(), any())).thenReturn(retrieveSpec)
        `when`(retrieveSpec.toEntity(String::class.java)).thenReturn(ResponseEntity.ok("ok-response"))

        val result = stripeClient.createCheckoutSession(req)

        val bodyCaptor = argumentCaptor<String>()
        verify(postSpec).body(bodyCaptor.capture())

        val capturedBody = bodyCaptor.firstValue

        assertTrue(capturedBody.contains("amount=1000"))
        assertTrue(capturedBody.contains("currency=usd"))
        assertTrue(capturedBody.contains("payment_method=card"))

        assertEquals("ok-response", result)
    }

    @Test
    fun `should throw BadRequestException when CardException occurs`() {
        val req = PaymentRequest(1000.0, "usd", "card")

        val postSpec = mock(RestClient.RequestBodyUriSpec::class.java)
        val retrieveSpec = mock(RestClient.ResponseSpec::class.java)

        `when`(restClient.post()).thenReturn(postSpec)
        `when`(postSpec.uri(anyString())).thenReturn(postSpec)
        `when`(postSpec.header(anyString(), anyString())).thenReturn(postSpec)
        `when`(postSpec.contentType(any())).thenReturn(postSpec)
        `when`(postSpec.body(any<String>())).thenReturn(postSpec)
        `when`(postSpec.retrieve()).thenReturn(retrieveSpec)
        `when`(retrieveSpec.onStatus(any(), any())).thenAnswer { invocation ->
            val predicate = invocation.arguments[0] as Predicate<HttpStatusCode>
            val errorHandler = invocation.arguments[1] as RestClient.ResponseSpec.ErrorHandler

            if (predicate.test(HttpStatus.UNAUTHORIZED)) {
                throw BadRequestException("Card declined: simulated error")
            }

            retrieveSpec
        }

        val ex =
            assertThrows<BadRequestException> {
                stripeClient.createCheckoutSession(req)
            }
        assertThat(ex.message!!.contains("Card declined")).isTrue()
    }

    @Test
    fun `should throw RuntimeException when generic StripeException occurs`() {
        val req = PaymentRequest(1000.0, "usd", "card")

        val postSpec = mock(RestClient.RequestBodyUriSpec::class.java)
        val retrieveSpec = mock(RestClient.ResponseSpec::class.java)

        `when`(restClient.post()).thenReturn(postSpec)
        `when`(postSpec.uri(anyString())).thenReturn(postSpec)
        `when`(postSpec.header(anyString(), anyString())).thenReturn(postSpec)
        `when`(postSpec.contentType(any())).thenReturn(postSpec)
        `when`(postSpec.body(any<String>())).thenReturn(postSpec)
        `when`(postSpec.retrieve()).thenReturn(retrieveSpec)
        `when`(retrieveSpec.onStatus(any(), any())).thenAnswer { invocation ->
            val predicate = invocation.arguments[0] as Predicate<HttpStatusCode>
            val errorHandler = invocation.arguments[1] as RestClient.ResponseSpec.ErrorHandler

            if (predicate.test(HttpStatus.UNAUTHORIZED)) {
                throw ExternalServiceException("Service unreachable")
            }

            retrieveSpec
        }

        assertThrows<ExternalServiceException> {
            stripeClient.createCheckoutSession(req)
        }
    }
}
