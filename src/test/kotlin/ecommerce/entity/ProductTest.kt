package ecommerce.entity

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ecommerce.repository.ProductJpaRepository
import org.springframework.beans.factory.annotation.Autowired

class ProductTest {

    @Autowired
    lateinit var productRepository: ProductJpaRepository

    @BeforeEach
    fun setUp() {
        val baseProduct = Product(
            name = "TestProduct",
            price = 19.99,
            imageUrl = "http://test.com/image.png",
            options = emptyList()
        )

        val option = Option(
            name = "Standard",
            quantity = 1,
            product = baseProduct
        )

        val productWithOptions = Product(
            name = "TestProduct",
            price = 19.99,
            imageUrl = "http://test.com/image.png",
            options = listOf(option)
        )
        val savedProduct = productRepository.save(productWithOptions)
    }

        @Test
    fun `addOption should throw error if option name is not unique`() {
        assertThrows<IllegalArgumentException> {
            val product = createProduct(listOf(option))
            product.addOption(option)
        }
    }

    @Test
    fun `throw error if list of options is empty`() {
        assertThrows<IllegalArgumentException> {
            createProduct()
        }
    }

    @Test
    fun `throw error if list of option has duplications`() {
        assertThrows<IllegalArgumentException> {
            createProduct(listOf(option, option))
        }
    }

    private fun createProduct(options: List<Option> = emptyList()): Product {
        return Product(
            name = "pizza",
            price = 1.2,
            imageUrl = "https://pizza.png",
            options = options
        )
    }

    companion object {
        val option: Option by lazy {
            val dummy = Product("x", 1.0, "url", emptyList())
            Option("salami", 1, dummy)
        }
    }
}