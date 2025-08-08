package ecommerce.repository

import ecommerce.entity.Option
import ecommerce.entity.Product
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class ProductJpaRepositoryTest {
    @Autowired
    lateinit var productJpaRepository: ProductJpaRepository

    private lateinit var savedProduct: Product

    @BeforeEach
    fun setUp() {
        val option =
            Option(
                name = "name",
                quantity = 1,
            )

        val product =
            Product(
                name = "TestProduct",
                price = 19.99,
                imageUrl = "http://test.com/image.png",
                options = listOf(option),
            )

        savedProduct = productJpaRepository.save(product)
    }

    @Test
    fun `existsByName should return true when product exists`() {
        val exists = productJpaRepository.existsByName(savedProduct.name.value)
        assertThat(exists).isTrue()
    }

    @Test
    fun `existsByName should return false when product does not exist`() {
        val exists = productJpaRepository.existsByName("NonExistingProduct")
        assertThat(exists).isFalse()
    }

    @Test
    fun `existsByNameAndIdNot should return false for same id`() {
        val result = productJpaRepository.existsByNameAndIdNot(savedProduct.name.value, savedProduct.id)
        assertThat(result).isFalse()
    }
}
