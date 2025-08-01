package ecommerce.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProductTest {
    @Test
    fun `addOption should throw error if option name it not unique`() {
        assertThrows<IllegalArgumentException> {
            createProduct(
                listOf<Option>(
                    option,
                ),
            ).addOption(option)
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
            createProduct(
                listOf<Option>(
                    option,
                    option,
                ),
            )
        }
    }

    private fun createProduct(options: List<Option> = emptyList<Option>()): Product {
        return Product(
            "pizza",
            1.2,
            "https://pizza.png",
            options,
        )
    }

    companion object {
        val option =
            Option(
                "salami",
                1,
            )
    }
}
