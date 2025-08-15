package ecommerce.config.advice

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import ecommerce.dto.error.ErrorResponse
import ecommerce.utils.exception.CartOperationException
import ecommerce.utils.exception.DuplicateProductNameException
import ecommerce.utils.exception.EntityNotFoundException
import ecommerce.utils.exception.LowStockException
import ecommerce.utils.exception.StripeException
import ecommerce.utils.exception.UnauthorisedUserException
import ecommerce.utils.exception.UserAlreadyExistsException
import ecommerce.utils.exception.UserCredentialException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEmptyResult(
        err: EntityNotFoundException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        return errorResponse(HttpStatus.NOT_FOUND, "${err.message}", request)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val rootCause = ex.cause
        val message =
            when (rootCause) {
                is MismatchedInputException -> {
                    val fieldName = rootCause.path?.firstOrNull()?.fieldName ?: "unknown"
                    "Missing or invalid value for field: '$fieldName'"
                }
                else -> "Invalid request payload"
            }
        return errorResponse(HttpStatus.BAD_REQUEST, message, request)
    }

    @ExceptionHandler(DuplicateProductNameException::class)
    fun handleDuplicateProductName(
        ex: DuplicateProductNameException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        return errorResponse(HttpStatus.CONFLICT, ex.message ?: "Duplicate product", request)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        return errorResponse(HttpStatus.BAD_REQUEST, errors, request)
    }

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExistsException(
        ex: UserAlreadyExistsException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        return errorResponse(HttpStatus.CONFLICT, ex.message ?: "Already exists", request)
    }

    @ExceptionHandler(UnauthorisedUserException::class)
    fun handleUnauthorisedUserException(
        ex: UnauthorisedUserException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        return errorResponse(HttpStatus.UNAUTHORIZED, ex.message ?: "UNAUTHORIZED", request)
    }

    @ExceptionHandler(UserCredentialException::class)
    fun handleUserCredentialException(
        ex: UserCredentialException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        return errorResponse(HttpStatus.UNAUTHORIZED, ex.message ?: "UNAUTHORIZED", request)
    }

    @ExceptionHandler(CartOperationException::class)
    fun handleCartOperationException(
        ex: CartOperationException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        return errorResponse(HttpStatus.CONFLICT, ex.message ?: "CONFLICT", request)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        return errorResponse(HttpStatus.BAD_REQUEST, ex.message ?: "BAD_REQUEST", request)
    }

    @ExceptionHandler(StripeException::class)
    fun handleStripeException(
        ex: StripeException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        return errorResponse(HttpStatus.BAD_REQUEST, ex.message ?: "BAD_REQUEST", request)
    }

    @ExceptionHandler(LowStockException::class)
    fun handleLowStockException(
        ex: LowStockException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        return errorResponse(HttpStatus.NOT_FOUND, ex.message ?: "NOT_FOUND", request)
    }

    private fun errorResponse(
        status: HttpStatus,
        message: Any,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val error = status.reasonPhrase
        val response =
            ErrorResponse(
                status = status.value(),
                error = error,
                message = message,
                path = request.requestURI,
            )
        return ResponseEntity.status(status).body(response)
    }
}
