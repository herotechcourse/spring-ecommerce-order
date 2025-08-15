package ecommerce.dto

import ecommerce.util.ValidationMessages
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class OptionDTO(
    @field:NotBlank(message = ValidationMessages.OPTION_NAME_REQUIRED)
    @field:Size(min = 1, max = 50, message = ValidationMessages.OPTION_NAME_SIZE)
    @field:Pattern(regexp = "^[a-zA-Z0-9 ()\\[\\]+\\-&/_]*$", message = ValidationMessages.OPTION_NAME_PATTERN)
    val name: String,
    @field:Size(min = 1, max = 99_999_999, message = ValidationMessages.OPTION_QUANTITY_SIZE)
    val quantity: Int,
    @field:NotBlank(message = ValidationMessages.OPTION_PRODUCT_ID_REQUIRED)
    val productId: Long? = null,
    @field:Positive(message = ValidationMessages.PRICE_POSITIVE)
    var unitPrice: Double,
    val id: Long = 0L,
)
