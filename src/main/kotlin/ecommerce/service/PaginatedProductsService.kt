package ecommerce.service

import ecommerce.dto.products.ProductResponseDTO
import ecommerce.repository.ProductRepository
import ecommerce.utils.extensions.toProductDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PaginatedProductsService(
    private val productRepository: ProductRepository,
) {
    fun getListProducts(
        page: Int,
        perPage: Int,
    ): Page<ProductResponseDTO> {
        require(page - 1 >= 0) { IllegalArgumentException("page must be > 0") }
        require(perPage > 1) { IllegalArgumentException("perPage must be > 1") }

        val pageable = PageRequest.of(page - 1, perPage)
        val products = productRepository.findAll(pageable)
        return products.map { it.toProductDTO() }
    }
}
