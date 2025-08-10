package ecommerce.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
class ExternalServiceException(override val message: String? = null) : RuntimeException()
