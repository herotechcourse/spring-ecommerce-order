package ecommerce.dto.payment

import ecommerce.model.Currency

class PaymentIntentRequest(
    val amount: Double,
    val currency: Currency,
    val paymentMethod: String,
)
