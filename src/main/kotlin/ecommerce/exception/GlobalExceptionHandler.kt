package ecommerce.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val errors =
            ex.bindingResult.fieldErrors.associate {
                it.field to (it.defaultMessage ?: "Invalid value")
            }
        return ResponseEntity.badRequest().body(errors)
    }

    @ExceptionHandler(ProductNotFoundException::class)
    fun handleNotFound(ex: ProductNotFoundException) = buildErrorResponse(HttpStatus.NOT_FOUND, ex)

    @ExceptionHandler(
        value = [
            RuntimeException::class,
            InsufficientQuantityException::class,
            IllegalArgumentException::class,
            CartException::class,
        ],
    )
    fun handleBadRequest(ex: RuntimeException) = buildErrorResponse(HttpStatus.BAD_REQUEST, ex)

    @ExceptionHandler(
        value = [
            ProductCreationException::class,
            ProductUpdateException::class,
            ElementNotFoundException::class,
            MemberNotFoundException::class,
            ProductDeleteException::class,
        ],
    )
    fun handleInternalServerError(ex: RuntimeException) = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex)

    @ExceptionHandler(
        value = [
            ProductAlreadyInDBException::class,
            MemberEmailAlreadyExistsException::class,
        ],
    )
    fun handleConflict(ex: RuntimeException) = buildErrorResponse(HttpStatus.CONFLICT, ex)

    @ExceptionHandler(
        value = [
            EmailOrPasswordIncorrectException::class,
            ForbiddenException::class,
        ],
    )
    fun handleForbidden(ex: RuntimeException) = buildErrorResponse(HttpStatus.FORBIDDEN, ex)

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(ex: UnauthorizedException) = buildErrorResponse(HttpStatus.UNAUTHORIZED, ex)

    @ExceptionHandler(StripeServerException::class)
    fun handleStripeServerPaymentError(ex: StripePaymentException) = buildErrorResponse(HttpStatus.BAD_GATEWAY, ex)

    fun buildErrorResponse(
        status: HttpStatus,
        ex: RuntimeException,
    ): ResponseEntity<ErrorMessageModel> {
        logger.error("Exception caught: ${ex.message}", ex)
        ex.cause?.let { cause ->
            logger.error("Caused by: ${cause.message}", cause)
        }
        val errorMessage = ErrorMessageModel(status.value(), ex.message)
        return ResponseEntity(errorMessage, status)
    }
}
