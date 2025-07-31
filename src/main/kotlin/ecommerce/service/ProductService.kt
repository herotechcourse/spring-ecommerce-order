package ecommerce.service

import ecommerce.dto.ProductRequest
import ecommerce.exception.DuplicateProductNameException
import ecommerce.model.Product
import ecommerce.repository.ProductRepository
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository,
) {
    fun getAll(): List<Product> {
        return productRepository.getAll()
    }

    fun getById(id: Long): Product? {
        return productRepository.findById(id)
    }

    fun create(request: ProductRequest): Product {
        if (productRepository.existsByName(request.name)) {
            throw DuplicateProductNameException()
        }

        val product =
            Product(
                id = 0L,
                name = request.name,
                price = request.price,
                imageUrl = request.imageUrl,
            )

        productRepository.createProduct(product)
        return product
    }

    fun update(
        id: Long,
        request: ProductRequest,
    ): Product {
        val existingProduct =
            productRepository.findById(id)
                ?: throw NoSuchElementException("Product not found")

        if (productRepository.existsByNameExcludingId(request.name, id)) {
            throw DuplicateProductNameException()
        }

        val updatedProduct =
            existingProduct.copy(
                name = request.name,
                price = request.price,
                imageUrl = request.imageUrl,
            )

        productRepository.updateProduct(updatedProduct)
        return updatedProduct
    }

    fun delete(id: Long) {
        productRepository.deleteProduct(id)
    }
}
