package ecommerce.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ProductNameTest {
    @Test
    fun `should create ProductName with valid value`() {
        val name = ProductName("Milk")
        assertEquals("Milk", name.value)
    }

    @Test
    fun `should throw if name is blank`() {
        assertThrows<IllegalArgumentException> {
            ProductName("   ")
        }
    }

    @Test
    fun `should throw if name is too long`() {
        assertThrows<IllegalArgumentException> {
            ProductName("a".repeat(16))
        }
    }

    @Test
    fun `should throw if name contains invalid characters`() {
        assertThrows<IllegalArgumentException> {
            ProductName("Milk!")
        }
    }

    @Test
    fun `should be equal if value is same`() {
        val n1 = ProductName("Milk")
        val n2 = ProductName("Milk")
        assertEquals(n1, n2)
    }
}
