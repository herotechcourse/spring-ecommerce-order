package ecommerce.advice

import ecommerce.exception.AuthorizationException
import ecommerce.exception.ForbiddenException
import ecommerce.exception.InsufficientStockException
import ecommerce.exception.InvalidCartItemQuantityException
import ecommerce.exception.InvalidOptionNameException
import ecommerce.exception.InvalidOptionQuantityException
import ecommerce.exception.NotFoundException
import ecommerce.exception.OperationFailedException
import ecommerce.exception.PaymentFailedException
import ecommerce.util.logger
import org.springframework.dao.DataAccessException
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.ResourceAccessException
import java.time.Instant

@RestControllerAdvice(annotations = [RestController::class])
class ApiErrorControllerAdvice {
    private val log = logger<ApiErrorControllerAdvice>()

    /**
     * Custom Exceptions
     */
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(e: NotFoundException): ResponseEntity<Map<String, Any>> {
        val errorMessage = e.message ?: "Not Found error"
        log.warn("NotFoundException occurred: $errorMessage", e)
        val body =
            mapOf(
                "status" to HttpStatus.NOT_FOUND.value(),
                "error" to "Operation failed",
                "message" to errorMessage,
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotSuchElementException(e: NoSuchElementException): ResponseEntity<Map<String, Any>> {
        val errorMessage = e.message ?: "Not such element"
        log.error("NoSuchElementException occurred: $errorMessage", e)
        val body =
            mapOf(
                "status" to HttpStatus.NOT_FOUND.value(),
                "error" to "Operation failed",
                "message" to errorMessage,
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body)
    }

    @ExceptionHandler(OperationFailedException::class)
    fun handleOperationFailedException(e: OperationFailedException): ResponseEntity<Map<String, Any>> {
        val errorMessage = e.message ?: "Operation failed"
        log.error("OperationFailedException: $errorMessage", e)
        val body =
            mapOf(
                "status" to HttpStatus.BAD_REQUEST.value(),
                "error" to "Operation failed",
                "message" to errorMessage,
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(AuthorizationException::class)
    fun handleAuthorizationException(e: AuthorizationException): ResponseEntity<Map<String, Any>> {
        val errorMessage = e.message ?: "Authorization failed"
        log.warn("AuthorizationException: $errorMessage", e)
        val body =
            mapOf(
                "status" to HttpStatus.UNAUTHORIZED.value(),
                "error" to "Authorization failed",
                "message" to errorMessage,
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body)
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(e: ForbiddenException): ResponseEntity<Map<String, Any>> {
        val errorMessage = e.message ?: "Invalid credentials"
        log.warn("ForbiddenException: $errorMessage", e)
        val body =
            mapOf(
                "status" to HttpStatus.FORBIDDEN.value(),
                "error" to "Authorization failed. Invalid Credentials",
                "message" to errorMessage,
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body)
    }

    @ExceptionHandler(InvalidCartItemQuantityException::class)
    fun handleInvalidCartItemQuantityException(e: InvalidCartItemQuantityException): ResponseEntity<Map<String, Any>> {
        val errorMessage = e.message ?: "Invalid quantity"
        log.warn("InvalidCartItemQuantityException: $errorMessage", e)
        val body =
            mapOf(
                "status" to HttpStatus.BAD_REQUEST.value(),
                "error" to "Invalid cart item quantity",
                "message" to errorMessage,
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(InvalidOptionNameException::class)
    fun handleInvalidOptionNameException(e: InvalidOptionNameException): ResponseEntity<Map<String, Any>> {
        val errorMessage = e.message ?: "Invalid option name"
        log.warn("InvalidOptionNameException: $errorMessage", e)
        val body =
            mapOf(
                "status" to HttpStatus.BAD_REQUEST.value(),
                "error" to "Invalid option name",
                "message" to errorMessage,
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(InvalidOptionQuantityException::class)
    fun handleInvalidOptionQuantityException(e: InvalidOptionQuantityException): ResponseEntity<Map<String, Any>> {
        val errorMessage = e.message ?: "Invalid option quantity"
        log.warn("InvalidOptionQuantityException: $errorMessage", e)
        val body =
            mapOf(
                "status" to HttpStatus.BAD_REQUEST.value(),
                "error" to "Invalid option quantity",
                "message" to errorMessage,
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(InsufficientStockException::class)
    fun handleInsufficientStockException(e: InsufficientStockException): ResponseEntity<Map<String, Any>> {
        val errorMessage = e.message ?: "Insufficient stock"
        log.warn("InsufficientStockException: $errorMessage", e)
        val body =
            mapOf(
                "status" to HttpStatus.CONFLICT.value(),
                "error" to "Insufficient stock",
                "message" to errorMessage,
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body)
    }

    @ExceptionHandler(PaymentFailedException::class)
    fun handlePaymentFailedException(e: PaymentFailedException): ResponseEntity<Map<String, Any>> {
        val errorMessage = e.message ?: "Payment failed"
        log.error("PaymentFailedException: $errorMessage", e)
        val body =
            mapOf(
                "status" to HttpStatus.BAD_REQUEST.value(),
                "error" to "Payment failed",
                "message" to errorMessage,
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    /**
     * Resource Access Exceptions: e.g., timeouts
     */
    @ExceptionHandler(ResourceAccessException::class)
    fun handleResourceAccessException(e: ResourceAccessException): ResponseEntity<Map<String, Any>> {
        val errorMessage = "Request timed out"
        log.error("ResourceAccessException: $errorMessage", e)
        val body =
            mapOf(
                "status" to HttpStatus.GATEWAY_TIMEOUT.value(),
                "error" to "Request Timeout",
                "message" to errorMessage,
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(body)
    }

    /**
     * Fallback handler for all uncaught exceptions
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<Map<String, Any>> {
        log.error("Unhandled exception occurred: ${e.message}", e)
        val body =
            mapOf(
                "status" to HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error" to "Internal Server Error",
                "message" to "An unexpected error occurred",
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }

    /**
     * JDBC Exceptions: DB errors
     */
    @ExceptionHandler(DataAccessException::class)
    fun handleDataAccessException(e: DataAccessException): ResponseEntity<Map<String, Any>> {
        val errorMessage = e.message ?: "Data Access Error"
        log.error("DataAccessException: $errorMessage", e)
        val body =
            mapOf(
                "status" to HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error" to "Empty result data access error",
                "message" to errorMessage,
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }

    @ExceptionHandler(DuplicateKeyException::class)
    fun handleDuplicateKeyException(e: DuplicateKeyException): ResponseEntity<Map<String, Any>> {
        val errorMessage = e.message ?: "Duplicate key error"
        log.warn("DuplicateKeyException: $errorMessage", e)
        val body =
            mapOf(
                "status" to HttpStatus.CONFLICT.value(),
                "error" to "Duplicate Key Error",
                "message" to errorMessage,
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body)
    }

    @ExceptionHandler(EmptyResultDataAccessException::class)
    fun handleEmptyResultException(e: EmptyResultDataAccessException): ResponseEntity<Map<String, Any>> {
        val errorMessage = e.message ?: "Empty result for your query"
        log.error("EmptyResultDataAccessException: $errorMessage", e)
        val body =
            mapOf(
                "status" to HttpStatus.NOT_FOUND.value(),
                "error" to "Empty Result Data Access",
                "message" to errorMessage,
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body)
    }

    /**
     * @Valid Exceptions, thrown when validation using jakarta fails.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        log.warn("Validation failed: ${e.message}")

        val errors = e.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Validation error") }

        val body =
            mapOf(
                "status" to HttpStatus.BAD_REQUEST.value(),
                "error" to "Validation failed",
                "message" to errors,
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }
}
