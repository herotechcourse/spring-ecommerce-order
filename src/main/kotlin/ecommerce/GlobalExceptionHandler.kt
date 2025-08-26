package ecommerce

import ecommerce.dto.errors.ErrorMessage
import ecommerce.dto.errors.ErrorResponse
import ecommerce.exception.AuthorizationException
import ecommerce.exception.InternalServerErrorException
import ecommerce.exception.MemberEmailAlreadyExistsException
import ecommerce.exception.NotFoundException
import ecommerce.exception.ProductNameAlreadyExistsException
import ecommerce.exception.StripeClientException
import ecommerce.util.logger
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = logger<GlobalExceptionHandler>()

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(e: NotFoundException): ResponseEntity<ErrorResponse> {
        val errorMessage = ErrorMessage("name", e.message)
        val errorResponse = ErrorResponse(listOf(errorMessage))
        logger.warn("NotFoundException occurred: $errorMessage", e)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(InternalServerErrorException::class)
    fun handleInternalServerErrorException(e: InternalServerErrorException): ResponseEntity<ErrorResponse> {
        val errorMessage = ErrorMessage("name", e.message)
        val errorResponse = ErrorResponse(listOf(errorMessage))
        logger.error("InternalServerErrorException occurred: $errorMessage", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    @ExceptionHandler(DataAccessException::class)
    fun handleDataAccessException(e: Exception): ResponseEntity<ErrorResponse> {
        val errorMessage = ErrorMessage("name", e.message)
        val errorResponse = ErrorResponse(listOf(errorMessage))
        logger.error("DataAccessException occurred: $errorMessage", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handlerIllegalStateException(e: Exception): ResponseEntity<ErrorResponse> {
        val errorMessage = ErrorMessage("name", e.message)
        val errorResponse = ErrorResponse(listOf(errorMessage))
        logger.error("IllegalStateException occurred: $errorMessage", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handlerIllegalArgumentException(e: Exception): ResponseEntity<ErrorResponse> {
        val errorMessage = ErrorMessage("name", e.message)
        val errorResponse = ErrorResponse(listOf(errorMessage))
        logger.warn("IllegalArgumentException occurred: $errorMessage", e)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors =
            e.bindingResult.fieldErrors.map { ErrorMessage(it.field, it.defaultMessage) }
        val errorResponse = ErrorResponse(errors)
        logger.warn("ValidationException occurred: $errors", e)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(AuthorizationException::class)
    fun handleAuthorizationException(e: AuthorizationException): ResponseEntity<ErrorResponse> {
        val error = ErrorMessage("authorization", e.message ?: "Authorization Error.")
        val errorResponse = ErrorResponse(listOf(error))
        logger.warn("AuthorizationException occurred: $error", e)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    @ExceptionHandler(ProductNameAlreadyExistsException::class)
    fun handleProductNameAlreadyExistsExceptionHandler(e: ProductNameAlreadyExistsException): ResponseEntity<ErrorResponse> {
        val errorMessage = ErrorMessage("name", e.message)
        val errorResponse = ErrorResponse(listOf(errorMessage))
        logger.warn("ProductNameAlreadyExistsException occurred: $errorMessage", e)
        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(MemberEmailAlreadyExistsException::class)
    fun handleMemberEmailAlreadyExistsExceptionHandler(e: MemberEmailAlreadyExistsException): ResponseEntity<ErrorResponse> {
        val errorMessage = ErrorMessage("name", e.message)
        val errorResponse = ErrorResponse(listOf(errorMessage))
        logger.warn("MemberEmailAlreadyExistsException occurred: $errorMessage", e)
        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(StripeClientException::class)
    fun handleStripeClientException(e: StripeClientException): ResponseEntity<ErrorResponse> {
        val stripeError = e.stripeErrorResponse.error
        val errorResponse =
            ErrorResponse(
                listOf(
                    ErrorMessage(
                        stripeError.code ?: "unknown_code",
                        stripeError.message,
                    ),
                ),
            )
        logger.warn("StripeClientException occurred: $stripeError", e)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
}
