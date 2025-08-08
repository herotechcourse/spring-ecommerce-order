package ecommerce.exception

import org.springframework.http.HttpStatus

class DuplicateProductNameException(
    override val field: String = "name",
    override val message: String = "Product name must be unique",
) : ApplicationException(field, message, HttpStatus.BAD_REQUEST)
