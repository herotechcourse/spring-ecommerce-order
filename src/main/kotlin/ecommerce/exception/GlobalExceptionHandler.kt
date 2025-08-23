package ecommerce.exception

import ecommerce.dto.ApiErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(PaymentDeclinedException::class)
    fun handlePaymentDeclined(e: PaymentDeclinedException): ResponseEntity<ApiErrorResponse> {
        val errorResponse = ApiErrorResponse(e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(PaymentProviderErrorException::class)
    fun handleStripeApi(e: PaymentProviderErrorException): ResponseEntity<ApiErrorResponse> {
        val errorResponse = ApiErrorResponse(e.message)
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        logger.warn("Validation exception occurred", ex)
        logger.warn("Validation failed: ${ex.bindingResult.fieldErrors}")
        val errors =
            ex.bindingResult.fieldErrors.map {
                FieldError(
                    field = it.field,
                    reason = "Invalid value",
                )
            }
        val response =
            ErrorResponse(
                message = "Validation failed",
                errors = errors,
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    @ExceptionHandler(ApplicationException::class)
    fun handleApplicationException(ex: ApplicationException): ResponseEntity<ErrorResponse> {
        logger.warn("Handled application exception", ex)
        val error =
            ErrorResponse(
                message = ex.message,
                errors = listOf(FieldError(ex.field, ex.message)),
            )
        return ResponseEntity.status(ex.status).body(error)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleUnexpectedException(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error occurred", ex)
        val error =
            ErrorResponse(
                message = "Internal server error",
                errors = listOf(FieldError("unknown", "Something went wrong")),
            )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}

data class ErrorResponse(
    val message: String,
    val errors: List<FieldError>,
)

data class FieldError(
    val field: String,
    val reason: String,
)
