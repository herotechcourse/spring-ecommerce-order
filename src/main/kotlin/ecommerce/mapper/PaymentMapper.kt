package ecommerce.mapper

import ecommerce.dto.PaymentResponse
import ecommerce.dto.StripePaymentResponse

class PaymentMapper

fun StripePaymentResponse.toPaymentResponse(): PaymentResponse {
    return PaymentResponse(id, amount / 100.0, status)
}
