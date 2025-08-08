package ecommerce.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class OptionNameTest {
    @Test
    fun `should create OptionName with valid value`() {
        val name = OptionName("Extra Cheese")
        assertEquals("Extra Cheese", name.value)
    }

    @Test
    fun `should throw if name is blank`() {
        assertThrows<IllegalArgumentException> {
            OptionName("   ")
        }
    }

    @Test
    fun `should throw if name is too long`() {
        val longName = "a".repeat(51)
        assertThrows<IllegalArgumentException> {
            OptionName(longName)
        }
    }

    @Test
    fun `should throw if name contains invalid characters`() {
        assertThrows<IllegalArgumentException> {
            OptionName("Cheese!")
        }
    }

    @Test
    fun `should be equal if values are the same`() {
        val name1 = OptionName("Extra Cheese")
        val name2 = OptionName("Extra Cheese")
        assertEquals(name1, name2)
    }
}
