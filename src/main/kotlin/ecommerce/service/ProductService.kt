package ecommerce.service

import ecommerce.dto.OptionResponse
import ecommerce.dto.ProductRequest
import ecommerce.entity.Option
import ecommerce.entity.Product
import ecommerce.exception.DuplicateProductNameException
import ecommerce.repository.OptionJpaRepository
import ecommerce.repository.ProductJpaRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductJpaRepository,
    private val optionRepository: OptionJpaRepository,
) {
    fun getAll(): List<Product> {
        return productRepository.findAll()
    }

    fun getById(id: Long): Product? {
        return productRepository.findById(id).orElse(null)
    }

    fun getAllPaginated(pageable: Pageable): Page<Product> {
        return productRepository.findAll(pageable)
    }

    fun getOptions(productId: Long): List<OptionResponse> {
        val option = optionRepository.findByProductId(productId)
        if (option.isEmpty()) throw NoSuchElementException("Not Found")
        return option.map {
            OptionResponse(
                it.id,
                it.name,
                it.quantity,
            )
        }
    }

    fun create(request: ProductRequest): Product {
        if (productRepository.existsByName(request.name)) {
            throw DuplicateProductNameException()
        }

        val options = request.options.map {
            Option(
                name = it.name,
                quantity = it.quantity,
            )
        }

        val product =
            Product(
                name = request.name,
                price = request.price,
                imageUrl = request.imageUrl,
                options = options
            )

        productRepository.save(product)
        return product
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

        val options = request.options.map {
            Option(
                name = it.name,
                quantity = it.quantity,
            )
        }

        val updatedProduct =
            Product(
                name = request.name,
                price = request.price,
                imageUrl = request.imageUrl,
                options = options
            )

        productRepository.save(updatedProduct)
        return updatedProduct
    }

    fun delete(id: Long) {
        productRepository.deleteById(id)
    }
}
