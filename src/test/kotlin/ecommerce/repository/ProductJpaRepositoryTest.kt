package ecommerce.repository

import ecommerce.entity.Member
import ecommerce.entity.Option
import ecommerce.entity.Product
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import kotlin.test.Test

@DataJpaTest
class ProductJpaRepositoryTest {
    @Autowired
    lateinit var productJpaRepository: ProductJpaRepository

    private lateinit var savedProduct: Product

    @BeforeEach
    fun setup() {
        val baseProduct = Product(
            name = "Widget",
            price = 9.99,
            imageUrl = "http://image.com/widget.png",
            options = emptyList()
        )

        val option = Option(
            name = "Standard",
            quantity = 1,
            product = baseProduct
        )

        val productWithOptions = Product(
            name = "Widget",
            price = 9.99,
            imageUrl = "http://image.com/widget.png",
            options = listOf(option)
        )

        savedProduct = productJpaRepository.save(productWithOptions)
    }

    @Test
    fun `existsByName should return true when product exists`() {
        val exists = productJpaRepository.existsByName(savedProduct.name)

        assertThat(exists).isTrue()
    }

    @Test
    fun `existsByName should return false when product does not exist`() {
        val exists = productJpaRepository.existsByName("NonExistingProduct")

        assertThat(exists).isFalse()
    }

    @Test
    fun `existsByNameAndIdNot should return false for same id`() {
        val result = productJpaRepository.existsByNameAndIdNot(savedProduct.name, savedProduct.id)

        assertThat(result).isFalse()
    }
}
