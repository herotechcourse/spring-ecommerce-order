package ecommerce.dto.options

import ecommerce.model.Option

data class OptionQuantityDto(
    val option: Option,
    val quantity: Int,
)
