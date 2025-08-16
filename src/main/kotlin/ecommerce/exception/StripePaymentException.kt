package ecommerce.exception

class StripePaymentException(
    message: String,
    cause: Throwable? = null,
    val code: String? = null,
    val declineCode: String? = null,
) : RuntimeException(message, cause)
