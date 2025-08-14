package ecommerce.exception

class StripeServerException(message: String, e: Exception) : RuntimeException(message, e)
