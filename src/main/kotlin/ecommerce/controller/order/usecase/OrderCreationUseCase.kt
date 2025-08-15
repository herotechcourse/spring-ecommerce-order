package ecommerce.controller.order.usecase

import ecommerce.dto.MemberLoginDTO
import ecommerce.dto.OrderDTO
import ecommerce.model.PaymentRequest

interface OrderCreationUseCase {
    fun create(
        memberLoginDTO: MemberLoginDTO,
        paymentRequest: PaymentRequest,
    ): OrderDTO
}
