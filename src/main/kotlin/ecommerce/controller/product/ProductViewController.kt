package ecommerce.controller.product

import ecommerce.annotation.IgnoreCheckLogin
import ecommerce.controller.product.usecase.CrudProductUseCase
import ecommerce.dto.ProductRequestDTO
import ecommerce.dto.ProductResponseDTO
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@Controller
class ProductViewController(private val crudProductUseCase: CrudProductUseCase) {
    @IgnoreCheckLogin
    @GetMapping
    fun showProducts(
        model: Model,
        @PageableDefault(size = 10, sort = ["name"], direction = Sort.Direction.DESC)
        pageable: Pageable,
    ): String {
        val products = crudProductUseCase.findAll(pageable)

        model.addAttribute("products", products.content)
        model.addAttribute("currentPage", products.number)
        model.addAttribute("totalPages", products.totalPages)
        model.addAttribute("productDTO", ProductResponseDTO(0L, "", 0.0, ""))
        model.addAttribute("hasErrors", false)
        return "product-list"
    }

    @PostMapping("/products")
    fun createProduct(
        @Valid productDTO: ProductRequestDTO,
        bindingResult: BindingResult,
        model: Model,
        @PageableDefault(size = 10, sort = ["name"], direction = Sort.Direction.DESC)
        pageable: Pageable,
    ): String {
        if (bindingResult.hasErrors()) {
            val products = crudProductUseCase.findAll(pageable)
            model.addAttribute("products", products.content)
            model.addAttribute("currentPage", products.number)
            model.addAttribute("totalPages", products.totalPages)
            model.addAttribute("productDTO", productDTO)
            model.addAttribute("hasErrors", bindingResult.hasErrors())
            return "product-list"
        }
        crudProductUseCase.save(productDTO)
        return "redirect:/"
    }
}
