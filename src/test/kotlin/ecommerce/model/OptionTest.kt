package ecommerce.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OptionTest {
    @Test
    fun `should create a new option`() {
        val option = Option("color", 10)
        assertThat(option).isNotNull
        assertThat(option.name).isEqualTo("color")
        assertThat(option.quantity).isEqualTo(10)
    }

    @Test
    fun `should throw error when name is too long`() {
        val name = "a".repeat(51)
        assertThrows<IllegalArgumentException> {
            Option(name, 10)
        }
    }

    @Test
    fun `should throw error when quantity is less than 1`() {
        assertThrows<IllegalArgumentException> {
            Option("color", 0)
        }
    }

    @Test
    fun `should throw error when quantity is more than 100 million`() {
        assertThrows<IllegalArgumentException> {
            Option("color", 100_000_001)
        }
    }

    @Test
    fun `should throw error when name contains invalid characters`() {
        assertThrows<IllegalArgumentException> {
            Option("color!", 10)
        }
    }

    @Test
    fun `should subtract quantity from option`() {
        val option = Option("color", 10)
        option.subtract(3)
        assertThat(option.quantity).isEqualTo(7)
    }
}
