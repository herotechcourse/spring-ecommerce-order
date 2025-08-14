package ecommerce.repository

import ecommerce.model.Option
import ecommerce.model.Product
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {
    @Autowired
    private lateinit var productJpaRepository: ProductJpaRepository

    @Test
    fun save() {
        val option = Option("test", 10)
        val expected = Product("test", 10.0.toBigDecimal(), "https://google.com", mutableListOf(option))
        val actual = productJpaRepository.save(expected)
        assertThat(actual.id).isNotNull
        assertThat(actual.id).isNotZero
        assertThat(actual.name).isEqualTo(expected.name)

        assertThat(actual.options).hasSize(1)
        assertThat(actual.options[0].name).isEqualTo("test")
        assertThat(actual.options[0].quantity).isEqualTo(10)
    }

    @Test
    fun findAll() {
        val option = Option("test", 10)
        val expected = Product("test", 10.0.toBigDecimal(), "https://google.com", mutableListOf(option))
        val expected2 = Product("test2", 10.0.toBigDecimal(), "https://google.com", mutableListOf(option))
        productJpaRepository.save(expected)
        productJpaRepository.save(expected2)
        val products = productJpaRepository.findAll()
        assertThat(products).hasSize(12)
    }

    @Test
    fun findFyId() {
        val product = productJpaRepository.findById(1).get()
        assertThat(product.name).isEqualTo("Espresso")
    }

    @Test
    fun existByName() {
        val option = Option("test", 10)
        val expected = Product("test", 10.0.toBigDecimal(), "https://google.com", mutableListOf(option))
        productJpaRepository.save(expected)
        val product = productJpaRepository.existsByName("test")
        assertThat(product).isTrue
    }
}
