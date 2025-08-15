package ecommerce.dto

import ecommerce.entities.PaymentEntity

class PaymentDTO(
    val id: Long? = null,
    val stripePaymentIntentId: String,
    val amount: Long,
    val currency: String,
    val status: PaymentEntity.PaymentStatus,
    val failureCode: String? = null,
    val failureMessage: String? = null,
    val orderId: Long,
)
