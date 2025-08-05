package ecommerce.service

import ecommerce.dto.OptionResponse
import ecommerce.dto.ProductRequest
import ecommerce.dto.ProductResponse
import ecommerce.entity.Option
import ecommerce.entity.Product
import ecommerce.exception.DuplicateProductNameException
import ecommerce.repository.OptionJpaRepository
import ecommerce.repository.ProductJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductJpaRepository,
    private val optionRepository: OptionJpaRepository,
) {
    @Transactional(readOnly = true)
    fun getAll(): List<ProductResponse> {
        return productRepository.findAll().map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): ProductResponse? {
        return productRepository.findById(id).orElse(null)?.toResponse()
    }

    @Transactional(readOnly = true)
    fun getAllPaginated(pageable: Pageable): Page<ProductResponse> {
        return productRepository.findAll(pageable).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
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

    @Transactional
    fun create(request: ProductRequest): ProductResponse {
        if (productRepository.existsByName(request.name)) {
            throw DuplicateProductNameException()
        }

        val product =
            productRepository.save(
                Product(
                    name = request.name,
                    price = request.price,
                    imageUrl = request.imageUrl,
                    options = emptyList(),
                ),
            )

        val options =
            request.options.map {
                Option(
                    name = it.name,
                    quantity = it.quantity,
                    product = product,
                )
            }

        val savedOptions = optionRepository.saveAll(options)

        return ProductResponse(
            id = product.id,
            name = product.name,
            price = product.price,
            imageUrl = product.imageUrl,
            options =
                savedOptions.map {
                    OptionResponse(
                        id = it.id,
                        name = it.name,
                        quantity = it.quantity,
                    )
                },
        )
    }

    @Transactional
    fun update(
        id: Long,
        request: ProductRequest,
    ): ProductResponse {
        val existingProduct =
            productRepository.findById(id).orElseThrow {
                NoSuchElementException("Product with ID $id not found.")
            }

        if (productRepository.existsByNameAndIdNot(request.name, id)) {
            throw DuplicateProductNameException()
        }

        val updatedProduct =
            Product(
                id = existingProduct.id,
                name = request.name,
                price = request.price,
                imageUrl = request.imageUrl,
                options = emptyList(),
            )

        val updatedOptions =
            request.options.map {
                Option(
                    name = it.name,
                    quantity = it.quantity,
                    product = updatedProduct,
                )
            }

        val productWithOptions =
            Product(
                id = existingProduct.id,
                name = request.name,
                price = request.price,
                imageUrl = request.imageUrl,
                options = updatedOptions,
            )

        productRepository.save(productWithOptions)
        return productWithOptions.toResponse()
    }

    private fun Product.toResponse(): ProductResponse {
        return ProductResponse(
            id = this.id,
            name = this.name,
            price = this.price,
            imageUrl = this.imageUrl,
            options =
                this.options.map {
                    OptionResponse(
                        id = it.id,
                        name = it.name,
                        quantity = it.quantity,
                    )
                },
        )
    }

    @Transactional
    fun delete(id: Long) {
        productRepository.deleteById(id)
    }
}
