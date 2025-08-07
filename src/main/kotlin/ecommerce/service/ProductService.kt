package ecommerce.service

import ecommerce.dto.OptionResponse
import ecommerce.dto.ProductRequest
import ecommerce.dto.ProductResponse
import ecommerce.exception.ConflictException
import ecommerce.exception.NotFoundException
import ecommerce.model.Option
import ecommerce.model.mapper.OptionMapper
import ecommerce.model.mapper.ProductMapper
import ecommerce.repository.OptionRepository
import ecommerce.repository.ProductRepository
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.min

@Transactional
@Service
class ProductService(private val productRepository: ProductRepository, private val optionRepository: OptionRepository) {
    fun create(productRequest: ProductRequest): Long {
        require(!productRequest.options.isNullOrEmpty()) { "options must not be empty" }

        if (productRepository.existsByName(productRequest.name)) {
            throw ConflictException("Product with name ${productRequest.name} already exists")
        }
        val product = productRequest.toProduct()
        productRequest.options.forEach {
            val option = Option(it.name, it.quantity)
            product.addOption(option)
        }
        val savedProduct = productRepository.save(product)
        return savedProduct.id
            ?: throw NotFoundException("Product with name ${productRequest.name} not found")
    }

    fun read(): List<ProductResponse> {
        val products = (productRepository.findAll()).map { ProductMapper.toProductDto(it) }
        return products
    }

    fun upsert(
        updateRequest: ProductRequest,
        id: Long,
    ): Boolean {
        if (!productRepository.existsById(id)) {
            create(updateRequest)
            return true
        }
        val product = productRepository.findById(id).orElseThrow { NotFoundException("Product with id $id not found") }
        product.name = updateRequest.name
        product.price = updateRequest.price
        product.imageUrl = updateRequest.imageUrl
        if (updateRequest.options != null) product.options.clear()
        updateRequest.options?.forEach {
            val option = Option(name = it.name, quantity = it.quantity)
            option.product = product
            product.options.add(option)
        }
        // study note: no save needed! Hibernate flushes changes at the end of a transaction!!
        return false
    }

    fun delete(id: Long) {
        productRepository.deleteById(id)
    }

    fun getPages(
        page: Int,
        size: Int,
    ): PageImpl<ProductResponse> {
        val products = productRepository.findAll().map { ProductMapper.toProductDto(it) }
        val pageRequest = PageRequest.of(page, size)
        val start = pageRequest.offset.toInt()
        val end = min(start + pageRequest.pageSize, products.size)

        val pageContent = products.subList(start, end)
        return PageImpl<ProductResponse>(pageContent, pageRequest, products.size.toLong())
    }

    fun findOptions(id: Long): List<OptionResponse> {
        val product = productRepository.findById(id).get()
        return product.options.map { OptionMapper.toOptionResponse(it) }
    }
}
