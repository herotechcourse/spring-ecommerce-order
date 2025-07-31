package ecommerce.mapper

import ecommerce.dto.ProductRequest
import ecommerce.model.Product

fun ProductRequest.toEntity(): Product {
    return Product(
        id = 0L,
        name = this.name,
        price = this.price,
        imageUrl = this.imageUrl,
    )
}
