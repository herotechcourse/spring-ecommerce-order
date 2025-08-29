package ecommerce.unit.model

import ecommerce.model.Product
import ecommerce.model.ProductOption
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class ProductOptionTest {
    private fun createTestProduct() =
        Product(
            name = "TestProduct",
            price = 10.0,
            quantity = 5,
            imageUrl = "https://example.com/image.jpg",
        )

    @Test
    fun `create product option successfully`() {
        assertDoesNotThrow {
            ProductOption(name = "valid name", quantity = 2, createTestProduct(), price = 15.0)
        }
    }

    @Test
    fun `subtract quantity successfully`() {
        val productOption = ProductOption(name = "Option", quantity = 10, createTestProduct(), price = 20.0)
        productOption.subtract(3)
        assertThat(productOption.quantity).isEqualTo(7)
    }

    @Test
    fun `subtract throws exception when quantity is less than 1`() {
        val productOption = ProductOption(name = "Option", quantity = 10, createTestProduct(), price = 20.0)
        assertThrows<IllegalArgumentException> {
            productOption.subtract(0)
        }
    }

    @Test
    fun `subtract throws exception when quantity exceeds stock`() {
        val productOption = ProductOption(name = "Option", quantity = 5, createTestProduct(), price = 25.0)
        assertThrows<IllegalArgumentException> {
            productOption.subtract(10)
        }
    }
}
