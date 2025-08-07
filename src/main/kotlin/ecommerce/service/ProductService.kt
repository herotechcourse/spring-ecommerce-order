package ecommerce.service

import ecommerce.dto.ProductRequest
import ecommerce.entity.OptionEntity
import ecommerce.entity.ProductEntity
import ecommerce.repository.ProductRepositoryJpa
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepositoryJpa: ProductRepositoryJpa,
) {
    fun createProduct(productRequest: ProductRequest): ProductEntity {
        if (productRepositoryJpa.existsByName(productRequest.name)) {
            throw IllegalArgumentException("Product with name '${productRequest.name}' already exists.")
        }
        val options =
            productRequest.options.map {
                OptionEntity(name = it.name, quantity = it.quantity)
            }.toMutableList()

        val product =
            ProductEntity(
                name = productRequest.name,
                price = productRequest.price,
                imageUrl = productRequest.imageUrl,
                options = options,
            )

        return productRepositoryJpa.save(product)
    }

    fun getAllProducts(
        page: Int,
        size: Int,
        sortBy: String = "name",
        direction: Sort.Direction = Sort.Direction.ASC,
    ): Page<ProductEntity> {
        val pageable = PageRequest.of(page, size, Sort.by(direction, sortBy))

        return productRepositoryJpa.findAll(pageable)
    }

    fun getProductsByPrice(
        price: Double,
        page: Int,
        size: Int,
    ): Page<ProductEntity> {
        val pageable = PageRequest.of(page, size)

        return productRepositoryJpa.findAllByPrice(price, pageable)
    }
}
