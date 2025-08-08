package ecommerce.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class MemberRequest(
    @field:NotBlank(message = "Email must not be blank")
    @field:Email(message = "Invalid email format")
    val email: String,
    @field:NotBlank(message = "Password must not be blank")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    val password: String,
)
