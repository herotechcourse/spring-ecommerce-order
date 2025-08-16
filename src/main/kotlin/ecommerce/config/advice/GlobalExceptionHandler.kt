package ecommerce.config.advice

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import ecommerce.dto.error.ErrorResponseDto
import ecommerce.exception.CartOperationException
import ecommerce.exception.DuplicateProductNameException
import ecommerce.exception.EntityNotFoundException
import ecommerce.exception.PaymentException
import ecommerce.exception.StripePaymentException
import ecommerce.exception.UnauthorisedUserException
import ecommerce.exception.UserAlreadyExistsException
import ecommerce.exception.UserCredentialException
import ecommerce.stripe.toUserFriendlyMessage
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
    ): ResponseEntity<ErrorResponseDto> {
        return errorResponse(HttpStatus.NOT_FOUND, "${err.message}", request)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponseDto> {
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
    ): ResponseEntity<ErrorResponseDto> {
        return errorResponse(HttpStatus.CONFLICT, ex.message ?: "Duplicate product", request)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponseDto> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid value") }
        return errorResponse(HttpStatus.BAD_REQUEST, errors, request)
    }

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExistsException(
        ex: UserAlreadyExistsException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponseDto> {
        return errorResponse(HttpStatus.CONFLICT, ex.message ?: "Already exists", request)
    }

    @ExceptionHandler(UnauthorisedUserException::class)
    fun handleUnauthorisedUserException(
        ex: UnauthorisedUserException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponseDto> {
        return errorResponse(HttpStatus.UNAUTHORIZED, ex.message ?: "UNAUTHORIZED", request)
    }

    @ExceptionHandler(UserCredentialException::class)
    fun handleUserCredentialException(
        ex: UserCredentialException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponseDto> {
        return errorResponse(HttpStatus.UNAUTHORIZED, ex.message ?: "UNAUTHORIZED", request)
    }

    @ExceptionHandler(CartOperationException::class)
    fun handleCartOperationException(
        ex: CartOperationException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponseDto> {
        return errorResponse(HttpStatus.CONFLICT, ex.message ?: "CONFLICT", request)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponseDto> {
        return errorResponse(HttpStatus.BAD_REQUEST, ex.message ?: "BAD_REQUEST", request)
    }

    private fun errorResponse(
        status: HttpStatus,
        message: Any,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponseDto> {
        val error = status.reasonPhrase
        val response =
            ErrorResponseDto(
                status = status.value(),
                error = error,
                message = message,
                path = request.requestURI,
            )
        return ResponseEntity.status(status).body(response)
    }

    @ExceptionHandler(PaymentException::class)
    fun handlePaymentException(
        ex: PaymentException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponseDto> {
        return errorResponse(
            HttpStatus.BAD_REQUEST,
            ex.message ?: "Payment failed",
            request,
        )
    }

    @ExceptionHandler(StripePaymentException::class)
    fun handleStripePaymentException(
        ex: StripePaymentException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponseDto> {
        val raw = ex.declineCode ?: ex.code ?: "payment_error"
        val msg = raw.toUserFriendlyMessage()
        return errorResponse(HttpStatus.BAD_REQUEST, msg, request)
    }

    @ExceptionHandler(Exception::class)
    fun handleAny(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponseDto> {
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request)
    }
}
