package ecommerce.dto

import ecommerce.util.ValidationMessages
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class ProductPatchDTO(
    @field:Size(min = 1, max = 15, message = ValidationMessages.PRODUCT_NAME_SIZE)
    @field:Pattern(regexp = "^[a-zA-Z0-9 ()\\[\\]+\\-&/_]*$", message = ValidationMessages.NAME_PATTERN)
    var name: String? = null,
    @field:Positive(message = ValidationMessages.PRICE_POSITIVE)
    var price: Double? = null,
    @field:Pattern(regexp = "^https?://.*$", message = ValidationMessages.IMAGE_FORMAT)
    var imageUrl: String? = null,
    val options: Set<OptionDTO> = emptySet(),
)

data class ProductRequestDTO(
    @field:NotBlank(message = ValidationMessages.NAME_REQUIRED)
    @field:Size(min = 1, max = 15, message = ValidationMessages.PRODUCT_NAME_SIZE)
    @field:Pattern(regexp = "^[a-zA-Z0-9 ()\\[\\]+\\-&/_]*$", message = ValidationMessages.NAME_PATTERN)
    var name: String,
    @field:NotNull(message = ValidationMessages.PRICE_REQUIRED)
    @field:Positive(message = ValidationMessages.PRICE_POSITIVE)
    var price: Double,
    @field:NotBlank(message = ValidationMessages.IMAGE_REQUIRED)
    @field:Pattern(regexp = "^https?://.*$", message = ValidationMessages.IMAGE_FORMAT)
    var imageUrl: String,
    @field:NotEmpty(message = ValidationMessages.OPTION_REQUIRED)
    val options: Set<OptionDTO> = emptySet(),
    var id: Long = 0L,
)

data class ProductResponseDTO(
    var id: Long,
    @field:NotBlank(message = ValidationMessages.NAME_REQUIRED)
    @field:Size(min = 1, max = 15, message = ValidationMessages.PRODUCT_NAME_SIZE)
    @field:Pattern(regexp = "^[a-zA-Z0-9 ()\\[\\]+\\-&/_]*$", message = ValidationMessages.NAME_PATTERN)
    var name: String,
    @field:NotNull(message = ValidationMessages.PRICE_REQUIRED)
    @field:Positive(message = ValidationMessages.PRICE_POSITIVE)
    var price: Double,
    @field:NotBlank(message = ValidationMessages.IMAGE_REQUIRED)
    @field:Pattern(regexp = "^https?://.*$", message = ValidationMessages.IMAGE_FORMAT)
    var imageUrl: String,
    @field:NotEmpty
    val options: List<OptionDTO> = emptyList(),
)
