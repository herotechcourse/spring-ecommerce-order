package ecommerce.util

import ecommerce.dto.CreateProductRequest
import ecommerce.dto.member.RegisterRequest
import ecommerce.model.Member
import ecommerce.model.Product

fun CreateProductRequest.toModel(id: Long? = null) =
    Product(
        name = name,
        price = price,
        quantity = quantity,
        imageUrl = imageUrl,
        id = id,
    )

fun RegisterRequest.toModel(hashedPassword: String) = Member(email, hashedPassword, name, role)
