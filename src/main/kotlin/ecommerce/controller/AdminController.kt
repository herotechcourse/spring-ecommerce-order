package ecommerce.controller

import ecommerce.dto.CreateProductRequest
import ecommerce.dto.ProductOptionRequest
import ecommerce.dto.ProductPatchRequest
import ecommerce.dto.UpdateProductRequest
import ecommerce.dto.auth.AuthenticatedUser
import ecommerce.model.Product
import ecommerce.model.ProductOption
import ecommerce.service.ProductOptionService
import ecommerce.service.ProductService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.net.URI

@RequestMapping("/api/admin/products")
@RestController
class AdminController(private val productService: ProductService, private val productOptionService: ProductOptionService) {
    @GetMapping("/{id}")
    fun getProductById(
        @PathVariable id: Long,
        user: AuthenticatedUser,
    ): Product {
        if (!user.isAdmin()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required")
        }
        return productService.findProductById(id)
    }

    @GetMapping("")
    fun getAllProducts(
        @RequestParam(defaultValue = "0")page: Int,
        @RequestParam(defaultValue = "10")size: Int,
        @RequestParam(defaultValue = "name")sortBy: String,
        user: AuthenticatedUser,
    ): Page<Product> {
        if (!user.isAdmin()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required")
        }
        return productService.findAllProducts(page, size, sortBy)
    }

    @PostMapping("")
    fun createProduct(
        @Valid @RequestBody productRequest: CreateProductRequest,
        user: AuthenticatedUser,
    ): ResponseEntity<Product> {
        if (!user.isAdmin()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required")
        }
        val saved = productService.createProduct(productRequest)
        return ResponseEntity.created(URI.create("/api/products/${saved.id}")).body(saved)
    }

    @PutMapping("/{id}")
    fun updateProduct(
        @Valid @RequestBody productRequest: UpdateProductRequest,
        @PathVariable id: Long,
        user: AuthenticatedUser,
    ): ResponseEntity<Product> {
        if (!user.isAdmin()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required")
        }
        val updated = productService.updateProduct(id, productRequest)
        return ResponseEntity.ok(updated)
    }

    @PatchMapping("/{id}")
    fun patchProduct(
        @Valid @RequestBody productRequest: ProductPatchRequest,
        @PathVariable id: Long,
        user: AuthenticatedUser,
    ): ResponseEntity<Product> {
        if (!user.isAdmin()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required")
        }
        val updated = productService.patchProduct(id, productRequest)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    fun deleteProductById(
        @PathVariable id: Long,
        user: AuthenticatedUser,
    ): ResponseEntity<Unit> {
        if (!user.isAdmin()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required")
        }
        productService.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("{productId}/options")
    fun addProductOption(
        @PathVariable productId: Long,
        @Valid @RequestBody productOptionRequest: ProductOptionRequest,
        user: AuthenticatedUser,
    ): ResponseEntity<ProductOption> {
        if (!user.isAdmin()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required")
        }
        val saved = productOptionService.saveProductOption(productId, productOptionRequest)
        return ResponseEntity
            .created(URI.create("/$productId/options/${saved.id}"))
            .body(saved)
    }

    @PutMapping("{productId}/options/{optionId}")
    fun updateOption(
        @PathVariable productId: Long,
        @PathVariable optionId: Long,
        @RequestBody productOptionRequest: ProductOptionRequest,
        user: AuthenticatedUser,
    ): ResponseEntity<ProductOption> {
        if (!user.isAdmin()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Admin privileges required")
        }
        val updated = productOptionService.saveProductOption(productId, productOptionRequest, optionId)
        return ResponseEntity
            .ok()
            .body(updated)
    }
}
