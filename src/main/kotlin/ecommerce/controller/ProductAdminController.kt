package ecommerce.controller

import ecommerce.model.Product
import ecommerce.repository.ProductJpaRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/products")
class ProductAdminController(
    private val productRepository: ProductJpaRepository,
) {
    @PostMapping(consumes = ["application/json"])
    @ResponseBody
    fun createProduct(
        @RequestBody product: Product,
    ): ResponseEntity<Product> {
        productRepository.save(product)
        return ResponseEntity.ok().build()
    }

    @GetMapping
    fun getProducts(model: Model): String {
        model.addAttribute("products", productRepository.findAll())
        return "table"
    }

    @GetMapping("/{id}")
    fun getProduct(
        @PathVariable id: Long,
    ): ResponseEntity<Product> {
        val product = productRepository.findById(id)
        return ResponseEntity.ok(product.get())
    }

    @PutMapping
    @ResponseBody
    fun updateProduct(
        @RequestBody product: Product,
    ): ResponseEntity<Product> {
        productRepository.save(product)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    fun deleteProduct(
        @PathVariable id: Long,
    ): ResponseEntity<Product> {
        productRepository.deleteById(id)
        return ResponseEntity.ok().build()
    }
}
