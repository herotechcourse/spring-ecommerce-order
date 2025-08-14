package ecommerce.mapper

import ecommerce.dto.ProductRequest
import ecommerce.dto.ProductResponse
import ecommerce.model.Product

class ProductMapper

fun Product.toDto(): ProductResponse = ProductResponse(id, name, price.toDouble(), imageUrl, options.map { it.toDto() })

fun ProductRequest.toEntity(): Product = Product(name, price.toBigDecimal(), imageUrl, options.map { it.toEntity() }.toMutableList())
