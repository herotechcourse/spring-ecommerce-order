package ecommerce.controller

import ecommerce.annotation.Admin
import ecommerce.dto.OptionResponse
import ecommerce.dto.PagedResponse
import ecommerce.dto.ProductRequest
import ecommerce.dto.ProductResponse
import ecommerce.dto.RegisteredMember
import ecommerce.service.mapper.ProductMapper
import ecommerce.service.ProductService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class ProductController(private val productService: ProductService) {
    @PostMapping("/api/products")
    fun create(
        @RequestBody @Valid product: ProductRequest,
        @Admin admin: RegisteredMember,
    ): ResponseEntity<Unit> {
        val id = productService.create(product)
        return ResponseEntity.created(URI.create("/api/products/$id")).build()
    }

    @GetMapping("/api/products")
    fun read(): ResponseEntity<List<ProductResponse>> {
        val products = productService.read()
        return ResponseEntity.ok().body(products)
    }

    @GetMapping("/api/products-page")
    fun asPages(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<PagedResponse<ProductResponse>> {
        val productPage = productService.getPages(page, size)

        val body = ProductMapper.toPagedResponse(productPage)

        return ResponseEntity.ok()
            .body(body)
    }

    @PutMapping("/api/products/{id}")
    fun upsert(
        @RequestBody @Valid newProduct: ProductRequest,
        @PathVariable id: Long,
        @Admin admin: RegisteredMember,
    ): ResponseEntity<Unit> {
        val created = productService.upsert(newProduct, id)
        return if (created) {
            return ResponseEntity.created(URI.create("/api/products/$id")).build()
        } else {
            ResponseEntity.ok().build()
        }
    }

    @GetMapping("/api/products/{id}/options")
    fun getOptions(
        @PathVariable id: Long,
    ): ResponseEntity<List<OptionResponse>> {
        val response = productService.findOptions(id)
        return ResponseEntity.ok().body(response)
    }

    @DeleteMapping("/api/products/{id}")
    fun delete(
        @PathVariable id: Long,
        @Admin admin: RegisteredMember,
    ): ResponseEntity<Unit> {
        productService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
