package ecommerce.controller

import ecommerce.dto.OptionResponse
import ecommerce.dto.ProductRequest
import ecommerce.dto.ProductResponse
import ecommerce.service.ProductService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/products")
class ProductRestController(
    private val productService: ProductService,
) {
    @GetMapping
    fun getAll(pageable: Pageable): ResponseEntity<Page<ProductResponse>> {
        val page = productService.getAllPaginated(pageable)
        return ResponseEntity.ok(page)
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: Long,
    ): ResponseEntity<ProductResponse> {
        val product =
            productService.getById(id)
                ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(product)
    }

    @GetMapping("/{id}/options")
    fun getOptions(
        @PathVariable id: Long,
    ): ResponseEntity<List<OptionResponse>> {
        val options = productService.getOptions(id)
        return ResponseEntity.ok(options)
    }

    @PostMapping
    fun create(
        @RequestBody @Valid request: ProductRequest,
    ): ResponseEntity<ProductResponse> {
        val createdProduct = productService.create(request)
        return ResponseEntity.ok(createdProduct)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody @Valid request: ProductRequest,
    ): ResponseEntity<ProductResponse> {
        val updatedProduct = productService.update(id, request)
        return ResponseEntity.ok(updatedProduct)
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        productService.delete(id)
        return ResponseEntity.ok().build()
    }
}
