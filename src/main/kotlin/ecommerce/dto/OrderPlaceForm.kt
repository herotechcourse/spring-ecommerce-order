package ecommerce.dto

import ecommerce.model.OrderCurrency
import jakarta.validation.constraints.NotEmpty

data class OrderPlaceForm(
    @field:NotEmpty(message = "Cart items are required")
    var cartItemIds: List<Long>,
    var paymentMethod: String = "pm_card_visa",
    var currency: String = OrderCurrency.USD.name,
)
