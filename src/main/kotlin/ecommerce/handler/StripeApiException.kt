package ecommerce.handler

open class StripeApiException(
    message: String? = null,
    val stripeErrorCode: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)

class StripePaymentFailedException(
    message: String? = null,
    stripeErrorCode: String? = null,
    cause: Throwable? = null
) : StripeApiException(message, stripeErrorCode, cause)

class StripeConnectionException(
    message: String? = null,
    cause: Throwable? = null
) : StripeApiException(message, null, cause)
