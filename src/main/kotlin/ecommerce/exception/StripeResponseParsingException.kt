package ecommerce.exception

class StripeResponseParsingException(message: String, cause: Throwable) :
    RuntimeException(message, cause)
