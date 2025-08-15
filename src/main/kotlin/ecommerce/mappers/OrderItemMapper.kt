package ecommerce.mappers

import ecommerce.entities.OrderItemEntity

fun OrderItemEntity.toDTO() =
    ecommerce.dto.OrderItemDTO(
        optionId = option.id,
        quantity = quantity,
        unitPrice = unitPrice,
        id = id,
    )
