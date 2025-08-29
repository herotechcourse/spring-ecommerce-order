package ecommerce.advice

import ecommerce.exception.AuthenticationException
import ecommerce.exception.AuthorizationException
import ecommerce.exception.DuplicateNameException
import ecommerce.exception.ErrorResponse
import ecommerce.exception.FailedPaymentException
import ecommerce.exception.InsufficientProductOptionsException
import ecommerce.exception.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalControllerAdvice {
    private val log = LoggerFactory.getLogger(GlobalControllerAdvice::class.java)

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleGenericException(e: Exception): ErrorResponse {
        log.error("Unhandled exception occurred", e)

        return ErrorResponse(
            error = "INTERNAL_SERVER_ERROR",
            message = "An unexpected error occurred. Please try again later.",
        )
    }

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundException(e: NotFoundException): ErrorResponse {
        return ErrorResponse(
            error = "NOT_FOUND",
            message = e.message ?: "Resource not found",
        )
    }

    @ExceptionHandler(AuthenticationException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleAuthenticationException(e: AuthenticationException): ErrorResponse {
        return ErrorResponse(
            error = "Unauthorized",
            message = e.message ?: "Authentication failed",
        )
    }

    @ExceptionHandler(AuthorizationException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleAuthorizationException(e: AuthorizationException): ErrorResponse {
        return ErrorResponse(
            error = "Forbidden",
            message = e.message ?: "Access denied",
        )
    }

    @ExceptionHandler(DuplicateNameException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDuplicateNameException(e: DuplicateNameException): ErrorResponse {
        return ErrorResponse(
            error = "CONFLICT",
            message = e.message ?: "Duplicate name conflict",
        )
    }

    @ExceptionHandler(FailedPaymentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleFailedPaymentException(e: FailedPaymentException): ErrorResponse {
        return ErrorResponse(
            error = "PAYMENT_FAILED",
            message = e.message ?: "Payment processing failed",
        )
    }

    @ExceptionHandler(IllegalStateException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleIllegalStateException(e: IllegalStateException): ErrorResponse {
        return ErrorResponse(
            error = "CONFLICT",
            message = e.message ?: "Invalid operation",
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationException(e: MethodArgumentNotValidException): ErrorResponse {
        val fieldErrors = e.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        return ErrorResponse(
            error = "VALIDATION_FAILED",
            message = "Request validation failed",
            fieldErrors = fieldErrors,
        )
    }

    @ExceptionHandler(value = [IllegalArgumentException::class, InsufficientProductOptionsException::class])
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(e: Exception): ErrorResponse {
        return ErrorResponse(
            error = "BAD_REQUEST",
            message = e.message ?: "Invalid request",
        )
    }
}
