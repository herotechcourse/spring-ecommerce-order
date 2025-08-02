package ecommerce.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OptionTest {

    private val dummyProduct = Product(
        name = "Test Product",
        price = 1.0,
        imageUrl = "http://test.com",
        options = emptyList()
    )

    @Test
    fun `throw error if name length is longer than 50 letters`() {
        assertThrows<IllegalArgumentException> {
            Option("a".repeat(51), 1, dummyProduct)
        }
    }

    @Test
    fun `throw error if name contains invalid characters`() {
        assertThrows<IllegalArgumentException> {
            Option("a?", 1, dummyProduct)
        }
    }

    @Test
    fun `throw error if option quantity is less than 1`() {
        assertThrows<IllegalArgumentException> {
            Option("a", 0, dummyProduct)
        }
    }

    @Test
    fun `throw error if option quantity is more than 100_000_000`() {
        assertThrows<IllegalArgumentException> {
            Option("a", 100_000_001, dummyProduct)
        }
    }
}