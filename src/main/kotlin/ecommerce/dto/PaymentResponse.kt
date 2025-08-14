package ecommerce.dto

data class PaymentResponse(
    val id: String,
    val amount: Double,
    val status: String,
    val errorMessage: String? = null,
)
