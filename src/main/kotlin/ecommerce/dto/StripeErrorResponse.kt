package ecommerce.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

@JsonIgnoreProperties(ignoreUnknown = true)
data class StripeErrorDto(
    val error: StripeErrorDetail,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class StripeErrorDetail(
    val code: String?,
    val message: String?,
    val param: String?,
    val type: String?,
    val adviceCode: String?, // or any other fields Stripe may send
)

fun mapStringToStripeErrorResponse(errorBody: String): StripeErrorDto {
    val mapper = jacksonObjectMapper()
    mapper.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    val stripeErrorResponse = mapper.readValue(errorBody, StripeErrorDto::class.java)
    return stripeErrorResponse
}
