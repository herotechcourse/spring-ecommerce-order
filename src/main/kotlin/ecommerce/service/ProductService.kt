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
            }

        val product =
            ProductEntity(
                name = productRequest.name,
                price = productRequest.price,
                imageUrl = productRequest.imageUrl,
                options = options.toMutableList(),
            )

        options.forEach { it.product = product }

        return productRepositoryJpa.save(product)
    }

    fun updateProduct(
        id: Long,
        request: ProductRequest,
    ): ProductEntity {
        val product =
            productRepositoryJpa.findById(id)
                .orElseThrow { NoSuchElementException("Product with id $id not found") }

        // Check for name uniqueness if name is being changed
        if (product.name != request.name && productRepositoryJpa.existsByName(request.name)) {
            throw IllegalArgumentException("Product with name '${request.name}' already exists.")
        }

        product.options.clear()
        // this will execute the deletes before adding new ones
        productRepositoryJpa.flush()

        // update product fields
        product.name = request.name
        product.price = request.price
        product.imageUrl = request.imageUrl

        // Clear old options and add new ones
        val newOptions =
            request.options.map {
                OptionEntity(name = it.name, quantity = it.quantity, product = product)
            }

        product.options.addAll(newOptions)

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

    fun deleteProduct(id: Long) {
        if (!productRepositoryJpa.existsById(id)) {
            throw NoSuchElementException("Product with id $id not found")
        }
        productRepositoryJpa.deleteById(id)
    }
}
