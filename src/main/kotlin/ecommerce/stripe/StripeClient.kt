package ecommerce.stripe

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ecommerce.annotations.StripeProperties
import ecommerce.dto.stripe.PaymentRequestDto
import ecommerce.dto.stripe.PaymentResponseDto
import ecommerce.exception.StripePaymentException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import kotlin.math.roundToLong

private data class StripeLastPaymentError(
    @JsonProperty("decline_code") val declineCode: String? = null,
)

private data class StripePaymentIntent(
    val id: String,
    val amount: Long,
    val currency: String,
    val status: String,
    @JsonProperty("last_payment_error") val lastPaymentError: StripeLastPaymentError? = null,
)

data class StripeErrorInfo(
    val message: String,
    val code: String? = null,
    val declineCode: String? = null,
)

@Component
class StripeClient(
    private val stripeProperties: StripeProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val rest = RestClient.create()
    private val mapper = jacksonObjectMapper()

    fun createCheckoutSession(req: PaymentRequestDto): PaymentResponseDto {
        val amountMinor = (req.amount * 100).roundToLong()

        val body =
            listOf(
                "amount=$amountMinor",
                "currency=${req.currency}",
                "payment_method=${req.paymentMethodId}",
                "confirm=true",
                "automatic_payment_methods[enabled]=true",
                "automatic_payment_methods[allow_redirects]=never",
            ).joinToString("&")

        return try {
            val resp =
                rest.post()
                    .uri("https://api.stripe.com/v1/payment_intents")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ${stripeProperties.secretKey}")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .toEntity(String::class.java)

            val pi = mapper.readValue(resp.body, StripePaymentIntent::class.java)
            PaymentResponseDto(
                id = pi.id,
                amount = pi.amount,
                currency = pi.currency,
                status = pi.status,
                declineCode = pi.lastPaymentError?.declineCode,
            )
        } catch (e: RestClientResponseException) {
            val info = parseStripeError(e.responseBodyAsString)
            log.error(
                "Stripe error status={} code={} decline={} msg={}",
                e.statusCode.value(),
                info.code,
                info.declineCode,
                info.message,
            )
            throw StripePaymentException(
                message = info.message,
                cause = e,
                code = info.code,
                declineCode = info.declineCode,
            )
        } catch (e: Exception) {
            log.error("Unexpected error calling Stripe: {}", e.message, e)
            throw StripePaymentException("Unexpected Stripe error: ${e.message}", e)
        }
    }

    fun parseStripeError(json: String?): StripeErrorInfo {
        return try {
            val root: JsonNode = mapper.readTree(json ?: "{}")
            val err = root.path("error")
            val message = err.path("message").asText("Stripe error")
            val code = err.path("code").takeIf { it.isTextual }?.asText()
            val decline = err.path("decline_code").takeIf { it.isTextual }?.asText()
            StripeErrorInfo(message = message, code = code, declineCode = decline)
        } catch (e: Exception) {
            StripeErrorInfo(message = "Unable to parse Stripe error: ${e.message}")
        }
    }
}
