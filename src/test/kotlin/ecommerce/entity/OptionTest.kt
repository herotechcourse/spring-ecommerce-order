package ecommerce.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OptionTest {
    @Test
    fun `throw error if option quantity is less than 1`() {
        assertThrows<IllegalArgumentException> {
            Option("a", 0)
        }
    }

    @Test
    fun `throw error if option quantity is more than 100_000_000`() {
        assertThrows<IllegalArgumentException> {
            Option("a", 100_000_001)
        }
    }
}
