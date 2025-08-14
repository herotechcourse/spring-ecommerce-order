package ecommerce.client

import com.fasterxml.jackson.databind.ObjectMapper
import ecommerce.configuration.StripeProperties
import ecommerce.dto.OrderRequest
import ecommerce.dto.PaymentResponse
import ecommerce.dto.StripePaymentResponse
import ecommerce.exception.GlobalExceptionHandler
import ecommerce.exception.StripeErrorInfo
import ecommerce.exception.StripePaymentException
import ecommerce.exception.StripeServerException
import ecommerce.mapper.toPaymentResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Component
class StripeClient(
    private val stripeProperties: StripeProperties,
) {
    private val restClient = RestClient.create()

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    fun createCheckoutSession(
        req: OrderRequest,
        amount: Long,
    ): PaymentResponse? {
        val body =
            listOf(
                "amount=$amount",
                "currency=${req.currency}",
                "payment_method=${req.paymentMethod}",
                "confirm=true",
                "automatic_payment_methods[enabled]=true",
                "automatic_payment_methods[allow_redirects]=never",
            ).joinToString("&")

        logger.debug(
            "Creating PaymentIntent amount={} {} method={}",
            amount,
            req.currency,
            req.paymentMethod,
        )

        return try {
            val response =
                restClient.post()
                    .uri(stripeProperties.CreatePaymentIntentUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ${stripeProperties.secretKey}")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .toEntity(StripePaymentResponse::class.java)

            val responseBody = requireNotNull(response.body)
            logger.info("PaymentIntent created id={} status={}", responseBody.id, responseBody.status)
            responseBody.toPaymentResponse()
        } catch (e: RestClientResponseException) {
            val errorInfo = parseStripeError(e.responseBodyAsString)
            logger.error("Stripe error status={} code={} msg={}", e.statusCode, errorInfo.code, errorInfo.message)
            throw StripePaymentException(
                "Stripe error: ${errorInfo.message} (code: ${errorInfo.code})",
                e,
            )
        } catch (e: Exception) {
            logger.error("Unexpected error calling Stripe: {}", e.message, e)
            throw StripeServerException("Unexpected Stripe error: ${e.message}", e)
        }
    }

    fun parseStripeError(json: String?): StripeErrorInfo {
        return try {
            val obj = ObjectMapper().readTree(json)
            StripeErrorInfo(
                message = obj.get("error").get("message").asText(),
                code = obj.get("error").get("code").asText(),
            )
        } catch (e: Exception) {
            StripeErrorInfo(message = "Unable to parse Stripe error: ${e.message}")
        }
    }
}
