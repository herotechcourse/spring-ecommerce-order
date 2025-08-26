package ecommerce.exception

class NotFoundException(override val message: String = "Resource was not found.") : RuntimeException(message)
