package ecommerce.mappers

import ecommerce.dto.product.OptionResponse
import ecommerce.dto.product.ProductResponse
import ecommerce.model.Option
import ecommerce.model.Product
import org.springframework.data.domain.Page

object ProductMapper {
    fun toResponse(product: Product): ProductResponse = ProductResponse(product.id, product.name, product.price, product.imageUrl)

    fun toOptionResponse(options: List<Option>): List<OptionResponse> = options.map { OptionResponse(it.id, it.name, it.quantity) }

    fun toProductPageResponse(page: Page<Product>): Page<ProductResponse> =
        page.map { ProductResponse(it.id, it.name, it.price, it.imageUrl) }
}
