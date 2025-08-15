package ecommerce.dto

import ecommerce.util.ValidationMessages
import jakarta.validation.constraints.Positive

data class WishItemRequestDTO(
    @field:Positive(message = ValidationMessages.ID_REQUIRED)
    val productId: Long,
)
