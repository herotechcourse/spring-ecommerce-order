package ecommerce.mappers

import ecommerce.dto.WishItemResponseDTO
import ecommerce.entities.WishItemEntity

fun WishItemEntity.toDTO() = WishItemResponseDTO(id, member.id, product.toDTO(), addedAt)
