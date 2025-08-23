package ecommerce.service

import ecommerce.entity.Option
import ecommerce.entity.Product
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@AutoConfigureTestEntityManager
class OptionServiceIntegrationTest {
    @Autowired
    private lateinit var optionService: OptionService

    @Autowired
    private lateinit var entityManager: TestEntityManager

    private lateinit var option: Option
    private val initialStock = 20

    @BeforeEach
    fun setup() {
        option = Option(name = "Red", quantity = initialStock)
        val product = Product(name = "Beanie", price = 1200, "http://test.png", listOf(option))
        entityManager.persist(product)
        entityManager.flush()
    }

    @Nested
    @DisplayName("Tests for decreaseQuantity()")
    inner class DecreaseQuantityTests {
        @Test
        fun `should decrease quantity when stock is sufficient`() {
            val quantityToDecrease = 5

            optionService.decreaseQuantity(option.id, quantityToDecrease)
            entityManager.flush()
            entityManager.clear()

            val updatedOption = entityManager.find(Option::class.java, option.id)
            assertThat(updatedOption.quantity).isEqualTo(initialStock - quantityToDecrease)
        }

        @Test
        fun `should throw IllegalStateException when stock is insufficient`() {
            val quantityToDecrease = initialStock + 1

            assertThrows<IllegalStateException> {
                optionService.decreaseQuantity(option.id, quantityToDecrease)
            }

            entityManager.clear()
            val finalOption = entityManager.find(Option::class.java, option.id)
            assertThat(finalOption.quantity).isEqualTo(initialStock)
        }

        @Test
        fun `should throw IllegalArgumentException for non-positive quantity`() {
            assertThrows<IllegalArgumentException> {
                optionService.decreaseQuantity(option.id, 0)
            }

            assertThrows<IllegalArgumentException> {
                optionService.decreaseQuantity(option.id, -5)
            }
        }
    }

    @Nested
    @DisplayName("Tests for increaseQuantity()")
    inner class IncreaseQuantityTests {
        @Test
        fun `should increase quantity for an existing option`() {
            val quantityToIncrease = 10

            optionService.increaseQuantity(option.id, quantityToIncrease)
            entityManager.flush()
            entityManager.clear()

            val updatedOption = entityManager.find(Option::class.java, option.id)
            assertThat(updatedOption.quantity).isEqualTo(initialStock + quantityToIncrease)
        }

        @Test
        fun `should throw NoSuchElementException for a non-existent option`() {
            val nonExistentId = 999L

            assertThrows<NoSuchElementException> {
                optionService.increaseQuantity(nonExistentId, 10)
            }
        }

        @Test
        fun `should throw IllegalArgumentException for non-positive quantity`() {
            assertThrows<IllegalArgumentException> {
                optionService.increaseQuantity(option.id, 0)
            }

            assertThrows<IllegalArgumentException> {
                optionService.increaseQuantity(option.id, -5)
            }
        }
    }
}
