package ecommerce.controller

import ecommerce.dto.auth.AuthenticatedUser
import ecommerce.model.Product
import ecommerce.model.Role
import ecommerce.service.ProductOptionService
import ecommerce.service.ProductService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class AdminControllerUnitTest {
    @Mock
    private lateinit var productService: ProductService

    @Mock
    private lateinit var productOptionService: ProductOptionService

    private lateinit var adminController: AdminController

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        adminController = AdminController(productService, productOptionService)
    }

    @Test
    fun `should return product when found`() {
        val productId = 1L
        val expectedProduct = Product("Test Product", 99.99, 10, "https://example.com/image.jpg", id = productId)

        `when`(productService.findProductById(productId)).thenReturn(expectedProduct)

        val adminUser = AuthenticatedUser(1L, Role.ADMIN, "admin@test.com", "Admin User")
        val result = adminController.getProductById(productId, adminUser)

        assertThat(result).isEqualTo(expectedProduct)
        verify(productService, times(1)).findProductById(productId)
    }

    @Test
    fun `should throw exception when product not found`() {
        val productId = 999L
        `when`(productService.findProductById(productId)).thenThrow(RuntimeException("Product not found"))

        val adminUser = AuthenticatedUser(1L, Role.ADMIN, "admin@test.com", "Admin User")
        assertThrows(RuntimeException::class.java) {
            adminController.getProductById(productId, adminUser)
        }
        verify(productService, times(1)).findProductById(productId)
    }

    @Test
    fun `should return paginated products`() {
        val products =
            listOf(
                Product("Product 1", 50.0, 5, "https://example.com/image1.jpg", id = 1L),
                Product("Product 2", 75.0, 3, "https://example.com/image2.jpg", id = 2L),
            )
        val page = PageImpl(products, PageRequest.of(0, 10), 2)

        `when`(productService.findAllProducts(0, 10, "name")).thenReturn(page)

        val adminUser = AuthenticatedUser(1L, Role.ADMIN, "admin@test.com", "Admin User")
        val result = adminController.getAllProducts(0, 10, "name", adminUser)

        assertThat(result).isEqualTo(page)
        assertThat(result.content.size).isEqualTo(2)
        verify(productService, times(1)).findAllProducts(0, 10, "name")
    }
}
