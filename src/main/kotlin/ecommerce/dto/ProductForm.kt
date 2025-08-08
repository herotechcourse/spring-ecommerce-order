package ecommerce.dto

import ecommerce.model.Option
import ecommerce.model.Product
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class ProductForm(
    @field:NotBlank(message = "Product name is required")
    @field:Pattern(regexp = "^[a-zA-Z0-9()\\[\\]+\\-&/_ ]+$", message = "Contains unallowed character")
    @field:Size(min = 1, max = 15, message = "Must be no more than 15 characters, including spaces")
    var name: String = String(),
    @field:Positive(message = "Product price must be greater than zero")
    var price: Double = Double.NaN,
    @field:NotBlank(message = "Product image URL is required")
    @field:Pattern(regexp = "^https?://.*", message = "Must start with 'https://'.")
    var imageUrl: String = String(),
) {
    companion object {
        fun toProduct(
            productForm: ProductForm,
            options: List<Option>,
        ): Product {
            return Product(name = productForm.name, price = productForm.price, imageUrl = productForm.imageUrl, options = options)
        }
    }
}
