package ecommerce.dto

class StripeErrorDetail(
    val code: String?,
    val decline_code: String?,
    val message: String,
)
