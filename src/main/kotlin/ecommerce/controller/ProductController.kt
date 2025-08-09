package ecommerce.controller

import ecommerce.dto.ProductRequest
import ecommerce.entity.ProductEntity
import ecommerce.service.ProductService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.net.URI

@Validated
@Controller
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService,
) {
    @PostMapping()
    @ResponseBody
    fun create(
        @Valid
        @RequestBody productRequest: ProductRequest,
    ): ResponseEntity<Void> {
        val product = productService.createProduct(productRequest)

        return ResponseEntity.created(URI.create("/products/${product.id}")).build()
    }

    @GetMapping()
    @ResponseBody
    fun readAll(
        @RequestParam page: Int,
        @RequestParam size: Int,
    ): Page<ProductEntity> {
        return productService.getAllProducts(page, size)
    }

    @PutMapping("/{id}")
    @ResponseBody
    fun update(
        @PathVariable("id") id: Long,
        @RequestBody @Valid productRequest: ProductRequest,
    ): ResponseEntity<Void> {
        productService.updateProduct(id, productRequest)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/?page=1&size=10")
    fun getAllProducts(
        @PageableDefault(size = 10, sort = ["name"]) pageable: Pageable,
    ): Page<ProductEntity> {
        return productService.getAllProducts(
            page = pageable.pageNumber,
            size = pageable.pageSize,
            sortBy = pageable.sort.firstOrNull()?.property ?: "name",
            direction = pageable.sort.firstOrNull()?.direction ?: Sort.Direction.ASC,
        )
    }

    @GetMapping("/price")
    fun getByPrice(
        @RequestParam price: Double,
        @RequestParam page: Int,
        @RequestParam size: Int,
    ): Page<ProductEntity> {
        return productService.getProductsByPrice(price, page, size)
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    fun delete(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        productService.deleteProduct(id)
        return ResponseEntity.noContent().build()
    }
}
