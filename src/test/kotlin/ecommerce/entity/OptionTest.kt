package ecommerce.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OptionTest {
    @Test
    fun `should create an Option successfully with valid arguments`() {
        val option = Option(name = "Large", quantity = 100)
        option.product = Product("Skirt", 5, "http://test.jpg", listOf(option))

        assertThat(option.name.value).isEqualTo("Large")
        assertThat(option.quantity).isEqualTo(100)
        assertThat(option.id).isEqualTo(0L)
    }

    @Test
    fun `constructor should throw exception when quantity is zero`() {
        assertThrows<IllegalArgumentException> {
            Option(name = "Medium", quantity = 0)
        }
    }

    @Test
    fun `constructor should throw exception when quantity is negative`() {
        assertThrows<IllegalArgumentException> {
            Option(name = "Medium", quantity = -10)
        }
    }

    @Test
    fun `constructor should throw exception when quantity is above the limit`() {
        assertThrows<IllegalArgumentException> {
            Option(name = "Medium", quantity = 100_000_000)
        }
    }
}

