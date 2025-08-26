package ecommerce.controller.api

import ecommerce.dto.product.OptionResponse
import ecommerce.dto.product.ProductForm
import ecommerce.dto.product.ProductResponse
import ecommerce.mappers.ProductMapper
import ecommerce.service.ProductService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
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
import java.net.URI

@RestController
@RequestMapping("/api/products")
class ProductController(private val productService: ProductService) {
    @PostMapping
    fun createProduct(
        @RequestBody @Valid productForm: ProductForm,
    ): ResponseEntity<ProductResponse> {
        val product = productService.insert(productForm)
        val uri = URI.create("/api/products/${product.id}")
        val productResponse = ProductMapper.toResponse(product)
        return ResponseEntity.created(uri).body(productResponse)
    }

    @GetMapping
    fun getProducts(
        @RequestParam(defaultValue = "0") pageNumber: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "name") sortBy: String,
    ): ResponseEntity<Page<ProductResponse>> {
        val productPage = productService.getPaginatedProducts(pageNumber, pageSize, sortBy)
        val productResponsePage = ProductMapper.toProductPageResponse(productPage)
        return ResponseEntity.ok(productResponsePage)
    }

    @GetMapping("{id}")
    fun getProduct(
        @PathVariable id: Long,
    ): ResponseEntity<ProductResponse> {
        val product = productService.findById(id)
        val productResponse = ProductMapper.toResponse(product)
        return ResponseEntity.ok(productResponse)
    }

    @GetMapping("{id}/options")
    fun getProductOptions(
        @PathVariable id: Long,
    ): ResponseEntity<List<OptionResponse>> {
        val product = productService.findById(id)
        val options = product.options
        val optionResponse = ProductMapper.toOptionResponse(options)
        return ResponseEntity.ok(optionResponse)
    }

    @PutMapping("{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @RequestBody @Valid productForm: ProductForm,
    ): ResponseEntity<ProductResponse> {
        val product = productService.update(productForm, id)
        val productResponse = ProductMapper.toResponse(product)
        return ResponseEntity.ok(productResponse)
    }

    @DeleteMapping("{id}")
    fun deleteProduct(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        productService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
