package ecommerce.service

import ecommerce.dto.ProductRequest
import ecommerce.entity.Product
import ecommerce.exception.DuplicateProductNameException
import ecommerce.repository.ProductJpaRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductJpaRepository,
) {
    fun getAll(): List<Product> {
        return productRepository.findAll()
    }

    fun getById(id: Long): Product? {
        return productRepository.findById(id).orElse(null)
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

        productRepository.save(product)
        return product
    }

    fun getAllPaginated(pageable: Pageable): Page<Product> {
        return productRepository.findAll(pageable)
    }

    @Transactional
    fun update(
        id: Long,
        request: ProductRequest,
    ): Product {
        val existingProduct =
            productRepository.findById(id)

        if (productRepository.existsByNameAndIdNot(request.name, id)) {
            throw DuplicateProductNameException()
        }

        val updatedProduct =
            Product(
                name = request.name,
                price = request.price,
                imageUrl = request.imageUrl,
            )

        productRepository.save(updatedProduct)
        return updatedProduct
    }

    fun delete(id: Long) {
        productRepository.deleteById(id)
    }
}
