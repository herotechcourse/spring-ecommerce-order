package ecommerce.repository

import ecommerce.model.Product
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.repository.findByIdOrNull

@DataJpaTest
class ProductRepositoryTest(
    @Autowired private val products: ProductRepository,
    @Autowired private val optionRepository: OptionRepository,
    @Autowired private val testEntityManager: TestEntityManager,
) {
    @AfterEach
    fun clean() {
        testEntityManager.clear()
    }

    @Test
    fun save() {
        val options = optionRepository.findAll().subList(0, 3)
        val expected = Product(name = "abc", price = 1.2, imageUrl = "https://sample.com/2", options = options)
        val actual = products.save(expected)
        assertThat(actual.id).isNotZero()
        assertThat(actual.name).isEqualTo(expected.name)
    }

    @Test
    fun findById() {
        val product = products.findAll().first()
        val actual = products.findById(product.id).get()
        assertThat(actual.id).isNotZero()
        assertThat(actual.id).isEqualTo(product.id)
        assertThat(actual.name).isEqualTo(product.name)
    }

    @Test
    fun findByName() {
        val product = products.findById(1L).get()
        val actual = products.findByName("Iron Man").get()
        assertThat(actual.id).isNotZero()
        assertThat(actual.id).isEqualTo(product.id)
        assertThat(actual.name).isEqualTo(product.name)
    }

    @Test
    fun `findAll - has some data`() {
        save()
        val actual = products.findAll()
        actual.size
        assertThat(actual).isNotEmpty()
        assertThat(actual).hasSize(8)
    }

    @Test
    fun update() {
        val product = products.findAll().first()

        val expectedName = "abc"
        val expectedPrice = 1.2
        val expectedImage = "https://sample.com/2"

        product.changeName(expectedName)
        product.changePrice(expectedPrice)
        product.changeImageUrl(expectedImage)

        val target = products.findById(product.id).get()

        assertThat(target.id).isEqualTo(product.id)
        assertThat(target.name).isEqualTo(product.name)
        assertThat(target.price).isEqualTo(product.price)
        assertThat(target.imageUrl).isEqualTo(product.imageUrl)
    }

    @Test
    fun delete() {
        val product = products.findAll().first()

        products.delete(product)

        val actual = products.findByIdOrNull(product.id)
        assertThat(actual).isNull()
    }

    @Test
    fun `findByName() - return true if a product with same name exists`() {
        val target = products.findByName("Iron Man")
        assertThat(target).isNotNull()
    }

    @Test
    fun `findByName() - throws an exception if a product with same name does not exist`() {
        assertThrows<NoSuchElementException> { products.findByName("Iron Body").get() }
    }
}
