package ecommerce.model

import ecommerce.dto.products.OptionDTO
import ecommerce.dto.products.OptionPatchDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class OptionTest {
    @ParameterizedTest
    @ValueSource(ints = [-1, 0, 100_000_001])
    fun `throws for invalid quantity`(quantity: Int) {
        assertThat(
            assertThrows<IllegalArgumentException> {
                Option(
                    "name",
                    10.0,
                    quantity,
                    "https://example.com",
                )
            }.message,
        ).isEqualTo("quantity must be positive and less than 100000000.")
    }

    @ParameterizedTest
    @ValueSource(doubles = [-1.0, 0.0, 0.001])
    fun `throws for invalid price`(price: Double) {
        assertThat(
            assertThrows<IllegalArgumentException> {
                Option(
                    "name",
                    price,
                    10,
                    "https://example.com",
                )
            }.message,
        ).isEqualTo("price must be greater than 0.01")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "      "])
    fun `throws for empty name`(name: String) {
        assertThat(
            assertThrows<IllegalArgumentException> {
                Option(
                    name,
                    10.0,
                    10,
                    "https://example.com",
                )
            }.message,
        ).isEqualTo("name must not be empty")
    }

    @Test
    fun `throws for long name`() {
        assertThat(
            assertThrows<IllegalArgumentException> {
                Option(
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                    10.0,
                    10,
                    "https://example.com",
                )
            }.message,
        ).isEqualTo("name must be less than 50 characters")
    }

    @ParameterizedTest
    @ValueSource(strings = ["$", "@" ])
    fun `throws for invalid character name`(name: String) {
        assertThat(
            assertThrows<IllegalArgumentException> {
                Option(
                    name,
                    10.0,
                    10,
                    "https://example.com",
                )
            }.message,
        ).isEqualTo("name must match pattern")
    }

    @ParameterizedTest
    @ValueSource(strings = ["www.google.com", "http:www.google.com"])
    fun `throws for invalid imageUrl`(imageUrl: String) {
        assertThat(
            assertThrows<IllegalArgumentException> {
                Option(
                    "name",
                    10.0,
                    10,
                    imageUrl,
                )
            }.message,
        ).isEqualTo("image URL must valid url")
    }

    @Test
    fun updateFields() {
        val optionDTO =
            OptionDTO(
                "name-1",
                10.1,
                11,
                "https://example-1.com",
            )
        val option =
            Option(
                "name",
                10.0,
                10,
                "https://example.com",
            )
        option.updateFields(optionDTO)

        assertThat(option.name).isEqualTo(optionDTO.name)
        assertThat(option.price).isEqualTo(optionDTO.price)
        assertThat(option.quantity).isEqualTo(optionDTO.quantity)
        assertThat(option.imageUrl).isEqualTo(optionDTO.imageUrl)
    }

    @Test
    fun `patchOption name`() {
        val optionPatchDTO = OptionPatchDTO(name = "patch")
        val option =
            Option(
                "name",
                10.0,
                10,
                "https://example.com",
            )
        option.patchOption(optionPatchDTO)
        assertThat(option.name).isEqualTo(optionPatchDTO.name)
    }

    @Test
    fun `patchOption price`() {
        val optionPatchDTO = OptionPatchDTO(price = 11.0)
        val option =
            Option(
                "name",
                10.0,
                10,
                "https://example.com",
            )
        option.patchOption(optionPatchDTO)
        assertThat(option.price).isEqualTo(optionPatchDTO.price)
    }

    @Test
    fun `patchOption quantity`() {
        val optionPatchDTO = OptionPatchDTO(quantity = 15)
        val option =
            Option(
                "name",
                10.0,
                10,
                "https://example.com",
            )
        option.patchOption(optionPatchDTO)
        assertThat(option.quantity).isEqualTo(optionPatchDTO.quantity)
    }

    @Test
    fun `patchOption imageUrl`() {
        val optionPatchDTO = OptionPatchDTO(imageUrl = "https://example-1.com")
        val option =
            Option(
                "name",
                10.0,
                10,
                "https://example.com",
            )
        option.patchOption(optionPatchDTO)
        assertThat(option.imageUrl).isEqualTo(optionPatchDTO.imageUrl)
    }
}
