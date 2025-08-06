package ecommerce.exception

import org.springframework.http.HttpStatus

open class ApplicationException(
    open val field: String = "unknown",
    override val message: String,
    val status: HttpStatus = HttpStatus.BAD_REQUEST,
) : Exception(message)
