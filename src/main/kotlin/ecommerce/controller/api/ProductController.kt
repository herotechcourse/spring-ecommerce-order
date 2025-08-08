package ecommerce.controller.api

import ecommerce.dto.OptionResponse
import ecommerce.dto.ProductForm
import ecommerce.dto.ProductResponse
import ecommerce.exception.ProductNameAlreadyExistsException
import ecommerce.model.Product
import ecommerce.service.ProductService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
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
        val productResponse = ProductResponse(product.id, product.name, product.price, product.imageUrl)
        return ResponseEntity.created(uri).body(productResponse)
    }

    @GetMapping
    fun getProducts(
        @RequestParam(defaultValue = "0") pageNumber: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "name") sortBy: String,
    ): ResponseEntity<Page<ProductResponse>> {
        val productPage =
            when (sortBy.isEmpty()) {
                true -> productService.getPaginatedProducts(pageNumber, pageSize)
                false -> productService.getPaginatedProducts(pageNumber, pageSize, sortBy)
            }
        val productResponsePage = productPage.map { ProductResponse(it.id, it.name, it.price, it.imageUrl) }
        return ResponseEntity.ok(productResponsePage)
    }

    @GetMapping("{id}")
    fun getProduct(
        @PathVariable id: Long,
    ): ResponseEntity<Product> {
        val product = productService.findById(id)
        return ResponseEntity.ok(product)
    }

    @GetMapping("{id}/options")
    fun getProductOptions(
        @PathVariable id: Long,
    ): ResponseEntity<List<OptionResponse>> {
        val product = productService.findById(id)
        val options =
            product.options
                .map { OptionResponse(it.id, it.name, it.quantity) }
        return ResponseEntity.ok(options)
    }

    @PutMapping("{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @RequestBody @Valid productForm: ProductForm,
    ): ResponseEntity<Product> {
        val product = productService.update(productForm, id)
        return ResponseEntity.ok(product)
    }

    @DeleteMapping("{id}")
    fun deleteProduct(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        productService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @ExceptionHandler(ProductNameAlreadyExistsException::class)
    fun handleProductNameAlreadyExistsExceptionHandler(e: Exception): ResponseEntity<Map<String, Any>> {
        val error = mapOf("name" to e.message)
        val errorBody = mapOf("errors" to error)
        println("ProductNameAlreadyExistsException occurred: $errorBody")
        return ResponseEntity.badRequest().body(errorBody)
    }
}
