package ecommerce.exception

class DuplicateProductNameException(
    val field: String = "name",
    override val message: String = "Product name must be unique",
) : RuntimeException()
