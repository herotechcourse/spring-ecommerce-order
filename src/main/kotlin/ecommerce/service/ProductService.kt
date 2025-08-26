package ecommerce.service

import ecommerce.dto.product.ProductForm
import ecommerce.exception.InternalServerErrorException
import ecommerce.exception.NotFoundException
import ecommerce.exception.ProductNameAlreadyExistsException
import ecommerce.model.Option
import ecommerce.model.Product
import ecommerce.repository.ProductRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository,
) {
    fun insert(form: ProductForm): Product {
        nameExists(form.name)

        val product = Product(name = form.name, price = form.price, imageUrl = form.imageUrl)
        val option = Option(name = "none", quantity = 1)
        product.addOption(option)

        val savedProduct = productRepository.save(product)
        return productRepository.findByIdOrNull(savedProduct.id)
            ?: throw InternalServerErrorException("ProductService.insert() - Product with ID ${savedProduct.id} not found")
    }

    fun getPaginatedProducts(
        pageNumber: Int,
        pageSize: Int,
        sortBy: String = "name",
    ): Page<Product> {
        val productPage =
            when (sortBy.isEmpty()) {
                true -> productRepository.findAll(PageRequest.of(pageNumber, pageSize))
                false -> productRepository.findAll(PageRequest.of(pageNumber, pageSize, Sort.by(sortBy)))
            }
        return productPage
    }

    fun findById(id: Long): Product = productRepository.findByIdOrNull(id) ?: throw NotFoundException(MESSAGE_PRODUCT_NOT_FOUND)

    fun update(
        form: ProductForm,
        id: Long,
    ): Product {
        val product =
            productRepository.findById(id).orElseThrow { NotFoundException(MESSAGE_PRODUCT_NOT_FOUND) }

        val originalName = product.name
        nameExists(form.name, originalName)

        product.changeName(form.name)
        product.changePrice(form.price)
        product.changeImageUrl(form.imageUrl)

        return productRepository.save(product)
    }

    fun delete(id: Long) {
        val product = productRepository.findByIdOrNull(id) ?: throw NotFoundException("Product not found - ID: $id")
        productRepository.delete(product)
    }

    private fun nameExists(
        name: String,
        originalName: String? = null,
    ) {
        if (originalName != null && name == originalName) {
            return
        } else if (productRepository.findByName(name).isPresent) {
            val message = "Product with name '$name' already exists."
            throw ProductNameAlreadyExistsException(message)
        }
    }

    companion object {
        const val MESSAGE_PRODUCT_NOT_FOUND = "Product not found"
    }
}
