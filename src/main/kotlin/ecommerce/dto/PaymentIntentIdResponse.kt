package ecommerce.dto

data class PaymentIntentIdResponse(val id: String)

// TODO: move to mapper
fun String.toPaymentIntentIdResponse() = PaymentIntentIdResponse(this)
