package ecommerce.dto

import ecommerce.util.ValidationMessages
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ActiveMemberDTO(
    val id: Long,
    @field:NotBlank(message = ValidationMessages.MEMBER_NAME_REQUIRED)
    val name: String,
    @field:Email(message = ValidationMessages.EMAIL_INVALID)
    val email: String,
)
