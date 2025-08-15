package ecommerce.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProductTest {
    @Test
    fun `options isNotEmpty()`() {
        assertThrows<IllegalArgumentException> {
            Product(
                "name",
                "http://example.com",
                mutableListOf(),
            )
        }
    }

    @Test
    fun `options name is unique`() {
        val options =
            mutableListOf(
                Option(
                    "name",
                    10.0,
                    10,
                    "https://example.com",
                ),
                Option(
                    "name",
                    10.0,
                    10,
                    "https://example.com",
                ),
            )
        assertThrows<IllegalArgumentException> {
            Product(
                "name",
                "http://example.com",
                options,
            )
        }
    }
}
