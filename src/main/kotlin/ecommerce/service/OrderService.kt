package ecommerce.service

import ecommerce.dto.cartProduct.CartProductDto
import ecommerce.enums.OrderStatus
import ecommerce.enums.PaymentOption
import ecommerce.model.MemberOrder
import ecommerce.model.OrderProduct
import ecommerce.model.User
import ecommerce.repository.MemberOrderRepository
import ecommerce.utils.exception.EntityNotFoundException
import ecommerce.utils.helper.OrderUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OrderService(
    val orderRepository: MemberOrderRepository,
) {
    fun createOrder(
        user: User,
        cartProductDto: List<CartProductDto>,
        paymentId: String,
    ): MemberOrder {
        return orderRepository.save(
            MemberOrder(
                cartProductDto.map {
                    OrderProduct(
                        it.optionId,
                        it.name,
                        it.price,
                        it.quantity,
                    )
                },
                user.id,
                user.email,
                paymentId,
                PaymentOption.STRIPE,
                OrderUtil.calculateTotal(cartProductDto),
                OrderStatus.PENDING,
            ),
        )
    }

    fun getUserOrders(userId: Long): List<MemberOrder> {
        return orderRepository.findAllByUserId(userId)
    }

    fun getOrderById(orderId: Long): MemberOrder {
        return orderRepository.findById(orderId).orElseThrow {
            throw EntityNotFoundException("Order with id $orderId not found")
        }
    }
}
