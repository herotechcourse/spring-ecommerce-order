package ecommerce.exception

import org.springframework.http.HttpStatus

class UnauthorizedException(
    override val message: String = "Unauthorized",
) : ApplicationException(field = "auth", message = message, status = HttpStatus.UNAUTHORIZED)
