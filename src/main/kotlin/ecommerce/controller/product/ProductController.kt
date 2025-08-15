package ecommerce.controller.product

import ecommerce.annotation.CheckAdminOnly
import ecommerce.annotation.IgnoreCheckLogin
import ecommerce.controller.product.usecase.CrudProductUseCase
import ecommerce.dto.ProductPatchDTO
import ecommerce.dto.ProductRequestDTO
import ecommerce.dto.ProductResponseDTO
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class ProductController(private val crudProductUseCase: CrudProductUseCase) {
    @IgnoreCheckLogin
    @GetMapping(PRODUCT_PATH)
    fun getProducts(
        @PageableDefault(size = 10, sort = ["name"], direction = Sort.Direction.ASC)
        pageable: Pageable,
    ): Page<ProductResponseDTO> = crudProductUseCase.findAll(pageable)

    @IgnoreCheckLogin
    @GetMapping(PRODUCT_PATH_ID)
    fun getProductById(
        @PathVariable id: Long,
    ): ResponseEntity<ProductResponseDTO> = ResponseEntity.ok(crudProductUseCase.findById(id))

    @CheckAdminOnly
    @PostMapping(PRODUCT_PATH)
    fun createProduct(
        @Valid @RequestBody productRequestDTO: ProductRequestDTO,
    ): ResponseEntity<ProductResponseDTO> {
        val saved = crudProductUseCase.save(productRequestDTO)
        return ResponseEntity.created(URI.create("$PRODUCT_PATH/${saved.id}")).body(saved)
    }

    @CheckAdminOnly
    @PutMapping(PRODUCT_PATH_ID)
    fun updateProductById(
        @Valid @RequestBody productDTO: ProductRequestDTO,
        @PathVariable id: Long,
    ): ResponseEntity<ProductResponseDTO> = ResponseEntity.ok(crudProductUseCase.updateById(id, productDTO))

    @CheckAdminOnly
    @PatchMapping(PRODUCT_PATH_ID)
    fun patchProductById(
        @Valid @RequestBody productPatchDTO: ProductPatchDTO,
        @PathVariable id: Long,
    ): ResponseEntity<ProductResponseDTO> = ResponseEntity.ok(crudProductUseCase.patchById(id, productPatchDTO))

    @CheckAdminOnly
    @DeleteMapping(PRODUCT_PATH_ID)
    fun deleteProductById(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        crudProductUseCase.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @CheckAdminOnly
    @DeleteMapping(PRODUCT_PATH)
    fun deleteAllProducts(): ResponseEntity<String> {
        crudProductUseCase.deleteAll()
        return ResponseEntity.noContent().build()
    }

    companion object {
        const val PRODUCT_PATH = "/api/products"
        const val PRODUCT_PATH_ID = "$PRODUCT_PATH/{id}"
    }
}
