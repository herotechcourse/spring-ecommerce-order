package ecommerce.dto

import ecommerce.entity.ProductEntity
import ecommerce.validation.UniqueProductName
import ecommerce.validation.ValidProductName
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ProductRequest(
    @field:NotBlank(message = "Product name must not be blank")
    @field:Size(min = 1, max = 15, message = "Product name must be between 1 and 15 characters")
    @field:ValidProductName
    @field:UniqueProductName
    val name: String,
    @field:DecimalMin("0.01", message = "Price must be positive")
    val price: Double,
    @field:Pattern(
        regexp = "^(http://|https://).*$",
        message = "Image URL must start with http:// or https://",
    )
    val imageUrl: String,
    @field:Valid
    val options: MutableList<OptionCreateDto> = mutableListOf(),
) {
    fun toProduct(): ProductEntity {
        val optionConverted = options.map { option -> option.toOptionEntity() }
        return ProductEntity(name = name, price = price, imageUrl = imageUrl, options = optionConverted)
    }
}
