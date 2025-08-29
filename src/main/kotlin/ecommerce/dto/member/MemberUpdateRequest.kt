package ecommerce.dto.member

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

class MemberUpdateRequest(
    @field:NotBlank(message = "Email must not be blank")
    @field:Email(message = "Email format is invalid")
    val email: String,
    @field:NotBlank(message = "Name must not be blank")
    val name: String,
)
