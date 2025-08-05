package ecommerce

import ecommerce.exception.AuthorizationException
import ecommerce.exception.InternalServerErrorException
import ecommerce.exception.NotFoundException
import io.jsonwebtoken.JwtException
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(Exception::class)
    fun handleGeneric(e: Exception): ResponseEntity<Void> {
        val error = mapOf("error" to e.message)
        val errorBody = mapOf("errors" to error)
        println("Unexpected Exception occurred: $errorBody")
        return ResponseEntity.internalServerError().build()
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(e: RuntimeException): ResponseEntity<Map<String, Any>> {
        val error = mapOf("error" to e.message)
        val errorBody = mapOf("errors" to error)
        println("RuntimeException occurred: $errorBody")
        return ResponseEntity.internalServerError().body(errorBody)
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(e: NotFoundException): ResponseEntity<Void> {
        println("NotFoundException occurred: " + e.message)
        return ResponseEntity.notFound().build()
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors =
            e.bindingResult.fieldErrors.associate { error ->
                error.field to (error.defaultMessage ?: "Invalid value")
            }
        val errorBody = mapOf("errors" to errors)
        println("MethodArgumentNotValidException occurred: $errorBody")
        return ResponseEntity.badRequest().body(errorBody)
    }

    @ExceptionHandler(InternalServerErrorException::class)
    fun handleInternalServerErrorException(e: InternalServerErrorException): ResponseEntity<Void> {
        println("InternalServerErrorException occurred: " + e.message)
        return ResponseEntity.internalServerError().build()
    }

    @ExceptionHandler(DataAccessException::class)
    fun handleDataAccessException(e: Exception): ResponseEntity<Void> {
        println("DataAccessException occurred: " + e.message)
        return ResponseEntity.internalServerError().build()
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handlerIllegalStateException(e: Exception): ResponseEntity<Void> {
        println("IllegalStateException occurred: " + e.message)
        return ResponseEntity.internalServerError().build()
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handlerIllegalArgumentException(e: Exception): ResponseEntity<Void> {
        println("IllegalArgumentException occurred: " + e.message)
        return ResponseEntity.internalServerError().build()
    }

    @ExceptionHandler(AuthorizationException::class)
    fun handleAuthorizationException(e: AuthorizationException): ResponseEntity<Map<String, Any>> {
        val error = mapOf("authorization" to e.message)
        val errorBody = mapOf("errors" to error)
        println("AuthorizationException occurred: $errorBody")
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
    }

    @ExceptionHandler(JwtException::class)
    fun handleJwtException(e: JwtException): ResponseEntity<Void> {
        println("JwtException occurred: " + e.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
    }
}
