package ecommerce.exception

class InternalServerErrorException(override val message: String = "Internal Server Error.") : RuntimeException(message)
