package ecommerce.dto.cart

import jakarta.validation.constraints.NotNull

class AddToCartRequest(
    @field:NotNull(message = "Product ID is required")
    val productOptionId: Long,
    @field:NotNull(message = "Quantity is required")
    val newProductOptionQuantity: Int,
    val cartItemId: Long?,
    @field:NotNull(message = "Cart is required")
    val cartId: Long,
)
