package ecommerce.exception

class ProductNameAlreadyExistsException(override val message: String = "Product name already exists.") : RuntimeException(message)
