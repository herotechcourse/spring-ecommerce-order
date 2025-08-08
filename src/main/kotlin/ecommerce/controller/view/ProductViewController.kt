package ecommerce.controller.view

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class ProductViewController {
    @GetMapping("/")
    fun displayMain(): String {
        return "redirect:/products"
    }

    @GetMapping("/products")
    fun displayProducts(): String {
        return "products"
    }

    @GetMapping("/add/product")
    fun displayCreateProductForm(): String {
        return "createProductForm"
    }

    @GetMapping("/update/product/{id}")
    fun displayUpdateProductForm(
        @PathVariable id: Long,
    ): String {
        return "updateProductForm"
    }
}
