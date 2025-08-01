package ecommerce.dto

import jakarta.validation.constraints.*

data class OptionRequest(
    @field:NotBlank
    @field:Size(max = 50)
    @field:Pattern(
        regexp = "^[a-zA-Z0-9 ()\\[\\]+\\-&/_]*\$",
        message = "Invalid characters in option name",
    )
    val name: String,
    @field:Min(1)
    @field:Max(99_999_999)
    val quantity: Int,
)
