package ecommerce.dto

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

data class PaymentIntentResponse(val id: String)

fun String.toPaymentIntentResponse(): PaymentIntentResponse {
    val mapper = jacksonObjectMapper()
    val rootNode: JsonNode = mapper.readTree(this)
    val id = rootNode["id"].asText()
    return PaymentIntentResponse(id)
}
