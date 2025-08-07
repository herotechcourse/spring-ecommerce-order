package ecommerce.controller

import ecommerce.dto.OptionResponse
import ecommerce.dto.ProductRequest
import ecommerce.dto.ProductResponse
import ecommerce.service.ProductService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/products")
class ProductRestController(
    private val productService: ProductService,
) {
    @GetMapping
    fun getAll(

        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "id") sortBy: String,
        @RequestParam(defaultValue = "asc") direction: String,
    ): ResponseEntity<Page<ProductResponse>> {
        val sort = if (direction.equals("desc", ignoreCase = true)) {
            Sort.by(sortBy).descending()
        } else {
            Sort.by(sortBy).ascending()
        }

        val pageable = PageRequest.of(page, size, sort)
        val result = productService.getAllPaginated(pageable)
        return ResponseEntity.ok(result)
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
