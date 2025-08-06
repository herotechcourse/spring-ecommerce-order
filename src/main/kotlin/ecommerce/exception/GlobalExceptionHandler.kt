package ecommerce.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors =
            ex.bindingResult.fieldErrors.map {
                FieldError(
                    field = it.field,
                    reason = it.defaultMessage ?: "Invalid value",
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
    fun handleApiException(ex: ApplicationException): ResponseEntity<ErrorResponse> {
        val error =
            ErrorResponse(
                message = ex.message,
                errors = listOf(FieldError(ex.field, ex.message)),
            )
        return ResponseEntity.status(ex.status).body(error)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleUnexpectedException(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        val error =
            ErrorResponse(
                message = "Internal server error",
                errors = listOf(FieldError("unknown", ex.message ?: "Something went wrong")),
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
