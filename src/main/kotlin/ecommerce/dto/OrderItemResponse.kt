package ecommerce.dto

import java.math.BigDecimal

class OrderItemResponse(
    val productName: String,
    val optionName: String,
    val quantity: Int,
    val price: BigDecimal,
)
