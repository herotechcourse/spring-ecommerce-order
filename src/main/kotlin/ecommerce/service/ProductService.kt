package ecommerce.service

import ecommerce.dto.CreateProductRequest
import ecommerce.dto.ProductPatchRequest
import ecommerce.dto.UpdateProductRequest
import ecommerce.exception.DuplicateNameException
import ecommerce.exception.InsufficientProductOptionsException
import ecommerce.exception.NotFoundException
import ecommerce.model.Product
import ecommerce.model.ProductOption
import ecommerce.repository.ProductOptionRepository
import ecommerce.repository.ProductRepository
import ecommerce.util.toModel
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val productOptionRepository: ProductOptionRepository,
) {
    fun findAllProducts(
        page: Int,
        size: Int,
        sortBy: String,
    ): Page<Product> {
        val pageable = PageRequest.of(page, size, Sort.by(sortBy))
        return productRepository.findAll(pageable)
    }

    fun findProductById(id: Long): Product {
        return productRepository.findByIdOrNull(id) ?: throw NotFoundException("Product with id $id not found")
    }

    @Transactional
    fun createProduct(request: CreateProductRequest): Product {
        if (request.productOptions.isEmpty()) {
            throw InsufficientProductOptionsException("Product needs at least one option")
        }

        if (productRepository.existsByName(request.name)) {
            throw DuplicateNameException("Product name already exists")
        }

        val product = request.toModel()
        val savedProduct = productRepository.save(product)

        val productOptions =
            request.productOptions.map { option ->
                option.productId = savedProduct.id!!
                ProductOption(option.name, option.quantity, savedProduct, request.price)
            }

        try {
            productOptionRepository.saveAll(productOptions)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to create product options: ${e.message}", e)
        }

        return savedProduct
    }

    @Transactional
    fun updateProduct(
        id: Long,
        request: UpdateProductRequest,
    ): Product {
        val existingProduct =
            productRepository.findByIdOrNull(id)
                ?: throw NotFoundException("Product with id $id not found")

        if (request.name != existingProduct.name && productRepository.existsByName(request.name)) {
            throw DuplicateNameException("Product name already exists")
        }

        existingProduct.updateProduct(request.name, request.price, request.quantity, request.imageUrl)
        return productRepository.save(existingProduct)
    }

    @Transactional
    fun patchProduct(
        id: Long,
        request: ProductPatchRequest,
    ): Product {
        val existingProduct =
            productRepository.findByIdOrNull(id)
                ?: throw NotFoundException("Product with id $id not found")

        request.name?.let { newName ->
            if (newName != existingProduct.name && productRepository.existsByName(newName)) {
                throw DuplicateNameException("Product name already exists")
            }
        }
        existingProduct.patchUpdate(request.name, request.price, request.quantity, request.imageUrl)
        return productRepository.save(existingProduct)
    }

    fun deleteById(id: Long) {
        productOptionRepository.deleteByProductId(id)
        productRepository.deleteById(id)
    }
}
