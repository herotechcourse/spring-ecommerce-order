package ecommerce.service

import ecommerce.dto.ProductForm
import ecommerce.exception.ProductNameAlreadyExistsException
import ecommerce.model.Product
import ecommerce.repository.ProductRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class ProductServiceTest(
    @Autowired private val productService: ProductService,
    @Autowired private val productRepository: ProductRepository,
) {
    fun insert(productName: String = "product1"): Product {
        val productForm = ProductForm(name = productName, price = 1.5, imageUrl = "https://www.product.com/image/1")
        return productService.insert(productForm)
    }

    @Test
    fun `insert() - should throw an exception when name of product already exists`() {
        val name = "Iron Man"
        val productForm = ProductForm(name = name, price = 1.5, imageUrl = "https://www.product.com/image/1")
        assertThrows<ProductNameAlreadyExistsException> { productService.insert(productForm) }
    }

    @Test
    fun `insert() - should insert and return the product when name of product not exists`() {
        val productForm = ProductForm(name = "Iron Body", price = 1.5, imageUrl = "https://www.product.com/image/1")
        val result = productService.insert(productForm)
        assertThat(result.id).isNotNull()
        assertThat(result.name).isEqualTo(productForm.name)
        assertThat(result.price).isEqualTo(productForm.price)
        assertThat(result.imageUrl).isEqualTo(productForm.imageUrl)
    }

    @Test
    fun `getPaginatedProducts() - should return number of products with page size`() {
        val products = productService.getPaginatedProducts(0, 5, "name")
        assertThat(products.size).isEqualTo(5)
    }

    @Test
    fun `getPaginatedProducts() - should throw an exception when page size is not valid`() {
        assertThrows<IllegalArgumentException> { productService.getPaginatedProducts(0, 0, "name") }
    }

    @Test
    fun `update() - should throw an exception when name of product already exists`() {
        val product = productRepository.findByName("Superman").get()
        val productForm = ProductForm(name = "Iron Man", price = 10.5, imageUrl = "https://www.product.com/image/1")
        assertThrows<ProductNameAlreadyExistsException> { productService.update(productForm, product.id) }
    }

    @Test
    fun `update() - should update the product when original product and new product have same name`() {
        val product = productRepository.findByName("Iron Man").get()
        val productForm = ProductForm(name = "Iron Man", price = 10.5, imageUrl = "https://www.product.com/image/1")
        val response = productService.update(productForm, product.id)
        assertThat(response.name).isEqualTo(productForm.name)
        assertThat(response.id).isEqualTo(product.id)
    }

    @Test
    fun `update() - should update the product when name of new product does not exists`() {
        val product = insert("Stone Body")
        val productForm = ProductForm(name = "Stone Body", price = 1.5, imageUrl = "https://www.product.com/image/1")
        val response = productService.update(productForm, product.id)
        assertThat(response.name).isEqualTo(productForm.name)
        assertThat(response.id).isEqualTo(product.id)
    }
}
