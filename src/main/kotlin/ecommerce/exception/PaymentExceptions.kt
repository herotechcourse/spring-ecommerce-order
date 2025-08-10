package ecommerce.exception

open class PaymentException(message: String) : RuntimeException(message)

class PaymentDeclinedException(message: String) : PaymentException(message)

class PaymentClientException(message: String) : PaymentException(message)

class PaymentServerException(message: String) : PaymentException(message)
