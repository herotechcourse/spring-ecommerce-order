package ecommerce.service

import ecommerce.dto.OptionCreateDto
import ecommerce.dto.ProductRequest
import ecommerce.entity.ProductEntity
import ecommerce.repository.ProductRepositoryJpa
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional // Ensures DB is rolled back after each test
class ProductServiceTest @Autowired constructor(
    private val productService: ProductService,
    private val productRepositoryJpa: ProductRepositoryJpa
) {

    private fun createProductEntity(
        name: String,
        price: Double
    ): ProductEntity {
        val request = ProductRequest(
            name = name,
            price = price,
            imageUrl = "http://image.com/$name",
            options = mutableListOf(OptionCreateDto("Option-$name", 2))
        )
        return productService.createProduct(request)
    }

    @Test
    fun `getAllProducts should return products in ascending order by default`() {
        createProductEntity("Product1", 1.0)
        createProductEntity("Product2", 22.0)
        createProductEntity("Product3", 45.0)

        val page = productService.getAllProducts(page = 0, size = 10)

        assertEquals(3, page.totalElements)
        assertEquals(listOf("Product1", "Product2", "Product3"), page.content.map { it.name })
    }

    @Test
    fun `getProductsByPrice should return only products with matching price`() {
        createProductEntity("Product1", 2.5)
        createProductEntity("Product2", 10.0)
        createProductEntity("Product3", 5.0)
        createProductEntity("Product4", 5.0)

        val page = productService.getProductsByPrice(price = 5.0, page = 0, size = 10)

        assertEquals(2, page.totalElements)
        assertTrue(page.content.all { it.price == 5.0 })
    }

    @Test
    fun `deleteProduct should remove product from database`() {
        val saved = createProductEntity("Product1", 8.0)

        assertTrue(productRepositoryJpa.existsById(saved.id!!))

        productService.deleteProduct(saved.id!!)

        assertFalse(productRepositoryJpa.existsById(saved.id!!))
    }

    @Test
    fun `deleteProduct should throw exception when product does not exist`() {
        val nonExistingId = 9999L

        val exception = assertThrows(NoSuchElementException::class.java) {
            productService.deleteProduct(nonExistingId)
        }

        assertEquals("Product with id $nonExistingId not found", exception.message)
    }
}
