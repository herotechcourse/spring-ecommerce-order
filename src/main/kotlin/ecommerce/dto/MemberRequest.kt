package ecommerce.dto

import jakarta.validation.constraints.NotBlank

data class MemberRequest(
    @field:NotBlank
    val email: String,
    @field:NotBlank
    val password: String,
)
