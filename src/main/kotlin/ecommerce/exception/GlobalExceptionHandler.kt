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

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(ex: UnauthorizedException): ResponseEntity<ErrorResponse> {
        val response =
            ErrorResponse(
                message = ex.message ?: "Unauthorized",
                errors = emptyList(),
            )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
    }

    @ExceptionHandler(DuplicateProductNameException::class)
    fun handleDuplicateProductName(ex: DuplicateProductNameException): ResponseEntity<ErrorResponse> {
        val error =
            ErrorResponse(
                message = "Validation failed",
                errors = listOf(FieldError(ex.field, ex.message ?: "Invalid value")),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
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
