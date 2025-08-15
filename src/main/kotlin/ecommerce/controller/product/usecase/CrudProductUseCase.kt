package ecommerce.controller.product.usecase

import ecommerce.dto.ProductPatchDTO
import ecommerce.dto.ProductRequestDTO
import ecommerce.dto.ProductResponseDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CrudProductUseCase {
    fun findAll(pageable: Pageable): Page<ProductResponseDTO>

    fun findById(id: Long): ProductResponseDTO

    fun save(productRequestDTO: ProductRequestDTO): ProductResponseDTO

    fun updateById(
        id: Long,
        productDTO: ProductRequestDTO,
    ): ProductResponseDTO

    fun patchById(
        id: Long,
        productPatchDTO: ProductPatchDTO,
    ): ProductResponseDTO

    fun deleteById(id: Long)

    fun deleteAll()

    fun validateProductNameUniqueness(name: String)
}
