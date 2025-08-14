package ecommerce.service

import ecommerce.dto.OptionDto
import ecommerce.dto.ProductPatchRequest
import ecommerce.dto.ProductRequest
import ecommerce.dto.ProductResponse
import ecommerce.exception.ProductCreationException
import ecommerce.exception.ProductDeleteException
import ecommerce.exception.ProductUpdateException
import ecommerce.mapper.toDto
import ecommerce.mapper.toEntity
import ecommerce.repository.ProductJpaRepository
import ecommerce.repository.existsByIdOrThrow
import ecommerce.repository.existsByNameOrThrow
import ecommerce.repository.getByIdOrThrow
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class ProductService(
    private val productJpaRepository: ProductJpaRepository,
) {
    fun findById(id: Long): ProductResponse {
        val product = productJpaRepository.getByIdOrThrow(id)
        return product.toDto()
    }

    fun findAll(
        page: Int,
        size: Int,
        sortBy: String,
        ascending: Boolean,
    ): Page<ProductResponse> {
        val sort = if (ascending) Sort.by(sortBy).ascending() else Sort.by(sortBy).descending()
        val pageable = PageRequest.of(page, size, sort)
        val products = productJpaRepository.findAll(pageable)
        return products.map { product -> product.toDto() }
    }

    fun createProduct(productRequest: ProductRequest): ProductResponse {
        productJpaRepository.existsByNameOrThrow(productRequest.name)
        try {
            val newProduct = productRequest.toEntity()
            newProduct.options.forEach { it.product = newProduct }
            val savedProduct = productJpaRepository.save(newProduct)
            return savedProduct.toDto()
        } catch (e: Exception) {
            throw ProductCreationException("Failed to create product", e)
        }
    }

    fun updateProduct(
        id: Long,
        productRequest: ProductPatchRequest,
    ): ProductResponse {
        val product = productJpaRepository.getByIdOrThrow(id)
        val newProduct =
            product.copy(
                name = productRequest.name ?: product.name,
                price = productRequest.price?.toBigDecimal() ?: product.price,
                imageUrl = productRequest.imageUrl ?: product.imageUrl,
            )
        try {
            productJpaRepository.save(newProduct)
            return newProduct.toDto()
        } catch (e: Exception) {
            throw ProductUpdateException("Failed to update product, id: $id", e)
        }
    }

    fun deleteProduct(id: Long) {
        productJpaRepository.existsByIdOrThrow(id)
        try {
            productJpaRepository.deleteById(id)
        } catch (e: Exception) {
            throw ProductDeleteException("Product not found, id: $id", e)
        }
    }

    fun addOptionToProduct(
        id: Long,
        option: OptionDto,
    ): ProductResponse {
        val product = productJpaRepository.getByIdOrThrow(id)
        product.addOption(option.toEntity())
        return product.toDto()
    }
}
