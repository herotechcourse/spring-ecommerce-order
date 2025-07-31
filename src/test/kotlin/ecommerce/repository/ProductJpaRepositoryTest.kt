package ecommerce.repository

import ecommerce.model.Product
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
    fun setUp() {
        val product = Product(
            name = "TestProduct",
            price = 19.99,
            imageUrl = "http://test.com/image.png"
        )
        savedProduct = productJpaRepository.save(product)
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
