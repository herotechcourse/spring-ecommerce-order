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
        val baseProduct = Product(
            name = "TestProduct",
            price = 19.99,
            imageUrl = "http://test.com/image.png",
            options = emptyList()
        )
        val persistedProduct = productJpaRepository.save(baseProduct)

        val option = Option(
            name = "name",
            quantity = 1,
            product = persistedProduct
        )

        val productWithOption = Product(
            name = persistedProduct.name,
            price = persistedProduct.price,
            imageUrl = persistedProduct.imageUrl,
            options = listOf(option),
            id = persistedProduct.id
        )
        savedProduct = productJpaRepository.save(productWithOption)
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