package ecommerce.exception

import com.fasterxml.jackson.annotation.JsonInclude
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
            )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
    }

    @ExceptionHandler(DuplicateProductNameException::class)
    fun handleDuplicateProductName(ex: DuplicateProductNameException): ResponseEntity<ErrorResponse> {
        val error =
            ErrorResponse(
                message = "Validation failed",
                errors = listOf(FieldError(ex.field, ex.message)),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    // (Step 2.1)

    @ExceptionHandler(PaymentDeclinedException::class)
    fun handleDeclined(ex: PaymentDeclinedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.PAYMENT_REQUIRED)
            .body(
                ErrorResponse(
                    message = ex.message ?: "Payment declined",
                    errors = emptyList(),
                    code = "PAYMENT_DECLINED",
                ),
            )
    }

    @ExceptionHandler(PaymentClientException::class)
    fun handleClientError(ex: PaymentClientException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    message = ex.message ?: "Invalid payment request",
                    errors = emptyList(),
                    code = "PAYMENT_CLIENT_ERROR",
                ),
            )
    }

    @ExceptionHandler(PaymentServerException::class)
    fun handleServerError(ex: PaymentServerException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(
                ErrorResponse(
                    message = ex.message ?: "Payment service unavailable",
                    errors = emptyList(),
                    code = "PAYMENT_SERVER_ERROR",
                ),
            )
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponse(
    val message: String,
    val errors: List<FieldError> = emptyList(),
    val code: String? = null,
)

data class FieldError(
    val field: String,
    val reason: String,
)
