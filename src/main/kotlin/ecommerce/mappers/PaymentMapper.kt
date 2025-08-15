package ecommerce.mappers

import ecommerce.dto.PaymentDTO
import ecommerce.entities.PaymentEntity

fun PaymentEntity.toDTO(): PaymentDTO {
    return PaymentDTO(
        id = this.id,
        stripePaymentIntentId = this.stripePaymentIntentId,
        amount = this.amount,
        currency = this.currency,
        status = this.status,
        failureCode = this.failureCode,
        failureMessage = this.failureMessage,
        orderId = this.order.id!!,
    )
}
