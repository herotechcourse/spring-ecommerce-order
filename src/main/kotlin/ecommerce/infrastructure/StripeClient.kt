package ecommerce.infrastructure

import ecommerce.model.StripeResponse
import org.springframework.http.MediaType
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange

@HttpExchange("/v1/payment_intents")
interface StripeClient {
    @PostExchange(
        contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        accept = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun createPaymentIntent(
        @RequestBody body: MultiValueMap<String, String>,
    ): StripeResponse
}
