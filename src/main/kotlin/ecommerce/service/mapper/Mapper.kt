package ecommerce.service.mapper

import ecommerce.dto.CartItemResponse
import ecommerce.dto.OptionResponse
import ecommerce.dto.OrderDto
import ecommerce.dto.OrderItemDto
import ecommerce.dto.PagedResponse
import ecommerce.dto.ProductResponse
import ecommerce.dto.RegisteredMember
import ecommerce.dto.Role
import ecommerce.model.CartItem
import ecommerce.model.Member
import ecommerce.model.Option
import ecommerce.model.Order
import ecommerce.model.OrderItem
import ecommerce.model.Product
import org.springframework.data.domain.PageImpl

object CartItemMapper {
    fun toResponse(cartItem: CartItem) =
        CartItemResponse(
            quantity = cartItem.quantity,
            productId = cartItem.option.id,
            productName = cartItem.option.name,
            productPrice = cartItem.option.product.price,
            productImageUrl = cartItem.option.product.imageUrl,
            optionId = cartItem.option.id,
            optionName = cartItem.option.name,
        )

    fun toPagedResponse(productPage: PageImpl<CartItemResponse>) =
        PagedResponse(
            productPage.content,
            productPage.number,
            productPage.size,
            productPage.totalPages,
            productPage.totalElements,
        )
}

object OptionMapper {
    fun toOptionResponse(option: Option) =
        OptionResponse(
            id = option.id,
            name = option.name,
            quantity = option.quantity,
        )
}

object ProductMapper {
    fun toProductResponse(entity: Product) =
        ProductResponse(
            id = entity.id,
            name = entity.name,
            price = entity.price,
            imageUrl = entity.imageUrl,
        )

    fun toPagedResponse(productPage: PageImpl<ProductResponse>) =
        PagedResponse(
            productPage.content,
            productPage.number,
            productPage.size,
            productPage.totalPages,
            productPage.totalElements,
        )
}

object MemberMapper {
    fun toRegisteredMember(member: Member): RegisteredMember {
        val role = Role.valueOf(member.role)
        return RegisteredMember(member.id!!, member.email, role)
    }
}

object OrderMapper {
    fun Order.toOrderDto(): OrderDto {
        val items = this.items.map { item -> item.toOrderItemDto() }
        return OrderDto(
            createdAt = this.createdAt,
            status = this.status.name,
            items = items,
            paymentAmount = this.paymentAmount,
            currency = this.currency,
            paymentMethod = this.paymentMethod,
            checkoutSessionId = this.checkoutSessionId,
        )
    }

    fun OrderItem.toOrderItemDto() =
        OrderItemDto(
            quantity = this.quantity,
            productName = this.productName,
            optionName = this.optionName,
        )
}
