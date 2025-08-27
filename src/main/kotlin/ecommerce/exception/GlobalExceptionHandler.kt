package ecommerce.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    private fun buildErrorMessage(ex: Throwable): String {
        return buildString {
            append(ex.message ?: "Unexpected error")
            ex.cause?.let { cause ->
                append(" | Cause: ")
                append(cause.message ?: cause.toString())
            }
        }
    }

    @ExceptionHandler(StripeResponseParsingException::class)
    fun handleStripeApiParsingException(ex: StripeResponseParsingException): ResponseEntity<ErrorMessageModel> {
        log.error("CRITICAL: Failed to parse Stripe API response. This may indicate an API change.", ex)

        val errorMessage =
            ErrorMessageModel(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred with our payment processor. Please contact support.",
            )
        return ResponseEntity(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(ProductNotFoundException::class)
    fun handleProductNotFound(ex: ProductNotFoundException): ResponseEntity<ErrorMessageModel> {
        val errorMessage =
            ErrorMessageModel(
                HttpStatus.NOT_FOUND.value(),
                buildErrorMessage(ex),
            )
        return ResponseEntity(errorMessage, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(
        value = [
            RuntimeException::class,
            InsufficientQuantityException::class,
        ],
    )
    fun handleRunTime(ex: RuntimeException): ResponseEntity<ErrorMessageModel> {
        val errorMessage =
            ErrorMessageModel(
                HttpStatus.BAD_REQUEST.value(),
                buildErrorMessage(ex),
            )
        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(
        value = [
            ElementNotFoundException::class,
            MemberNotFoundException::class,
            ProductIdNotFoundException::class,
        ],
    )
    fun handleNotFoundExceptions(ex: RuntimeException): ResponseEntity<ErrorMessageModel> {
        val errorMessage =
            ErrorMessageModel(
                HttpStatus.NOT_FOUND.value(),
                buildErrorMessage(ex),
            )
        return ResponseEntity(errorMessage, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(
        value = [
            ProductCreationException::class,
            ProductUpdateException::class,
        ],
    )
    fun handleServerErrorExceptions(ex: RuntimeException): ResponseEntity<ErrorMessageModel> {
        val errorMessage =
            ErrorMessageModel(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                buildErrorMessage(ex),
            )
        return ResponseEntity(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val errors =
            ex.bindingResult.fieldErrors.associate {
                it.field to (it.defaultMessage ?: "Invalid value")
            }
        return ResponseEntity.badRequest().body(errors)
    }

    @ExceptionHandler(
        value = [
            ProductAlreadyInDbException::class,
            MemberEmailAlreadyExistsException::class,
        ],
    )
    fun handleAlreadyInDbException(ex: RuntimeException): ResponseEntity<ErrorMessageModel> {
        val errorMessage =
            ErrorMessageModel(
                HttpStatus.CONFLICT.value(),
                buildErrorMessage(ex),
            )
        return ResponseEntity(errorMessage, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(
        value = [
            EmailOrPasswordIncorrectException::class,
            ForbiddenException::class,
        ],
    )
    fun handleEmailOrPasswordIncorrectException(ex: RuntimeException): ResponseEntity<ErrorMessageModel> {
        val errorMessage =
            ErrorMessageModel(
                HttpStatus.FORBIDDEN.value(),
                buildErrorMessage(ex),
            )
        return ResponseEntity(errorMessage, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(ex: UnauthorizedException): ResponseEntity<ErrorMessageModel> {
        val errorMessage =
            ErrorMessageModel(
                HttpStatus.UNAUTHORIZED.value(),
                buildErrorMessage(ex),
            )
        return ResponseEntity(errorMessage, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorMessageModel> {
        val errorMessage =
            ErrorMessageModel(
                HttpStatus.BAD_REQUEST.value(),
                buildErrorMessage(ex),
            )
        return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
    }
}
