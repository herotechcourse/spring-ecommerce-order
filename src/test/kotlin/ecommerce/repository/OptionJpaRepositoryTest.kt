package ecommerce.repository

import ecommerce.entity.Option
import ecommerce.entity.Product
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
class OptionJpaRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var optionRepo: OptionJpaRepository

    private lateinit var product: Product
    private lateinit var option: Option

    @BeforeEach
    fun setUp() {
        option = Option(name = "Large", quantity = 50)
        product = Product(
            name = "T-Shirt",
            price = 2500,
            imageUrl = "https://example.com/image.jpg",
            options = listOf(option)
        )

        entityManager.persist(product)
        entityManager.flush()
    }

    @Test
    fun `decrementStockIfEnough should decrease quantity when stock is sufficient`() {
        val updatedRows = optionRepo.decrementStockIfEnough(option.id, 10)

        assertThat(updatedRows).isEqualTo(1)

        entityManager.clear()
        val updatedOption = optionRepo.findById(option.id).get()

        assertThat(updatedOption.quantity).isEqualTo(40)
    }

    @Test
    fun `decrementStockIfEnough should not decrease quantity when stock is insufficient`() {
        val updatedRows = optionRepo.decrementStockIfEnough(option.id, 51)

        assertThat(updatedRows).isEqualTo(0)

        entityManager.clear()
        val updatedOption = optionRepo.findById(option.id).get()

        assertThat(updatedOption.quantity).isEqualTo(50)
    }

    @Test
    fun `incrementStock should increase quantity`() {
        val updatedRows = optionRepo.incrementStock(option.id, 20)

        assertThat(updatedRows).isEqualTo(1)

        entityManager.clear()
        val updatedOption = optionRepo.findById(option.id).get()

        assertThat(updatedOption.quantity).isEqualTo(70)
    }

    @Test
    fun `findByProductId should return all options for a given product`() {
        val option2 = Option(name = "Medium", quantity = 30)
        product.addOption(option2)
        entityManager.persistAndFlush(product)

        val foundOptions = optionRepo.findByProductId(product.id)

        assertThat(foundOptions).hasSize(2)
        assertThat(foundOptions).extracting("name").containsExactlyInAnyOrder("Large", "Medium")
    }
}
