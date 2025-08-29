package ecommerce.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProductTest {
    private val dummyProduct =
        Product(
            name = "Test Product",
            price = 1,
            imageUrl = "http://test.com",
            options =
                listOf(
                    Option("salami", 2),
                ),
        )

    @Test
    fun `addOption should throw error if option name is not unique`() {
        assertThrows<IllegalArgumentException> {
            dummyProduct.addOption(Option("salami", 2))
        }
    }

    @Test
    fun `price zero should fail validation`() {
        assertThrows<IllegalArgumentException> {
            Product(
                "a",
                0,
                "http://example.jpg",
                listOf(
                    Option("salami", 2),
                ),
            )
        }
    }

    @Test
    fun `link to image without http prefix should fail validation`() {
        assertThrows<IllegalArgumentException> {
            Product(
                "a",
                3,
                "example.jpg",
                listOf(
                    Option("salami", 2),
                ),
            )
        }
    }
}
