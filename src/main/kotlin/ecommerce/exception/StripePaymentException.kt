package ecommerce.exception

class StripePaymentException(
    override val message: String,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)
