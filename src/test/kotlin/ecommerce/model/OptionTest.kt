package ecommerce.model

import groovy.cli.Option
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class OptionTest {
    @Test
    fun `Throws exception when Option name has length more than 50`() {
        val name = "a".repeat(NAME_LENGTH_MAX_LIMIT_PLUS_ONE)
        val quantity = QUANTITY_MIN_VALUE

        assertThrows<IllegalArgumentException> { Option(name, quantity) }
    }

    @Test
    fun `Throws exception when Option quantity is not in between 1 and 100,000,000`() {
        val name = "test"
        val invalidQuantity1 = QUANTITY_MIN_VALUE_MINUS_ONE
        val invalidQuantity2 = QUANTITY_MIN_VALUE_MINUS_TWO
        val invalidQuantity3 = QUANTITY_MAX_VALUE_PLUS_ONE

        assertThrows<IllegalArgumentException> { Option(name, invalidQuantity1) }
        assertThrows<IllegalArgumentException> { Option(name, invalidQuantity2) }
        assertThrows<IllegalArgumentException> { Option(name, invalidQuantity3) }
    }

    @Test
    fun `Throws exception when Option name contains invalid characters`() {
        val invalidName1 = "test*"
        val invalidName2 = "test="
        val invalidName3 = "test'"
        val invalidName4 = "\"test"
        val invalidName5 = "{test"
        val invalidName6 = "test}"
        val invalidName7 = "^test"
        val quantity = QUANTITY_MIN_VALUE

        assertThrows<IllegalArgumentException> { Option(invalidName1, quantity) }
        assertThrows<IllegalArgumentException> { Option(invalidName2, quantity) }
        assertThrows<IllegalArgumentException> { Option(invalidName3, quantity) }
        assertThrows<IllegalArgumentException> { Option(invalidName4, quantity) }
        assertThrows<IllegalArgumentException> { Option(invalidName5, quantity) }
        assertThrows<IllegalArgumentException> { Option(invalidName6, quantity) }
        assertThrows<IllegalArgumentException> { Option(invalidName7, quantity) }
    }

    @Test
    fun `Does NOT throws exception when Option name contains valid characters`() {
        val invalidName1 = "test()"
        val invalidName2 = "test[]"
        val invalidName3 = "test+"
        val invalidName4 = "-test"
        val invalidName5 = "_test"
        val invalidName6 = "test/"
        val invalidName7 = "&test"
        val invalidName8 = "&test()[]+-/_test"
        val quantity = QUANTITY_MIN_VALUE

        assertDoesNotThrow { Option(invalidName1, quantity) }
        assertDoesNotThrow { Option(invalidName2, quantity) }
        assertDoesNotThrow { Option(invalidName3, quantity) }
        assertDoesNotThrow { Option(invalidName4, quantity) }
        assertDoesNotThrow { Option(invalidName5, quantity) }
        assertDoesNotThrow { Option(invalidName6, quantity) }
        assertDoesNotThrow { Option(invalidName7, quantity) }
        assertDoesNotThrow { Option(invalidName8, quantity) }
    }

    companion object {
        const val NAME_LENGTH_MAX_LIMIT = 50
        const val QUANTITY_MIN_VALUE = 1
        const val QUANTITY_MAX_VALUE = 100_000_000
        const val NAME_LENGTH_MAX_LIMIT_PLUS_ONE = NAME_LENGTH_MAX_LIMIT + 1
        const val QUANTITY_MIN_VALUE_MINUS_ONE = QUANTITY_MIN_VALUE - 1
        const val QUANTITY_MIN_VALUE_MINUS_TWO = QUANTITY_MIN_VALUE - 2
        const val QUANTITY_MAX_VALUE_PLUS_ONE = QUANTITY_MAX_VALUE + 1
    }
}
