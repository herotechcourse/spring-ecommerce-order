package ecommerce.service

import ecommerce.dto.order.OrderItemRequest
import ecommerce.dto.order.OrderRequest
import ecommerce.dto.order.OrderResponse
import ecommerce.exception.NotFoundException
import ecommerce.exception.StripeClientException
import ecommerce.model.Member
import ecommerce.model.Order
import ecommerce.model.OrderItem
import ecommerce.model.OrderStatus
import ecommerce.model.Payment
import ecommerce.model.Product
import ecommerce.repository.CartItemRepository
import ecommerce.repository.OptionRepository
import ecommerce.repository.OrderRepository
import ecommerce.repository.ProductRepository
import ecommerce.stripe.StripeClient
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrderService(
    private val stripeClient: StripeClient,
    private val productRepository: ProductRepository,
    private val optionRepository: OptionRepository,
    private val orderRepository: OrderRepository,
    private val cartItemRepository: CartItemRepository,
    private val paymentService: PaymentService,
) {
    @Transactional
    fun placeOrder(
        member: Member,
        req: OrderRequest,
    ): OrderResponse {
        val orderItems = req.items.map { createOrderItem(it) }
        val totalAmount = orderItems.sumOf { calculateAmount(it.product.price, it.quantity) }

//        val paymentRequest =
//            PaymentRequest(
//                amount = totalAmount,
//                currency = req.currency.code,
//                paymentMethod = req.paymentMethod,
//            )

        val paymentResponse =
            paymentService.processPayment(
                amount = totalAmount,
                currencyCode = req.currency.code,
                paymentMethod = req.paymentMethod,
            )

        if (paymentResponse.id.isBlank()) {
            throw StripeClientException.from(paymentResponse.errorMessage ?: "Stripe did not return a valid response.")
        }

        orderItems.forEach { item ->
            item.option.reduceStock(item.quantity)
            optionRepository.save(item.option)
            removeCartItem(member, item.product)
        }

        val order =
            Order(
                member = member,
                status = OrderStatus.PAID,
                createdAt = LocalDateTime.now(),
            )

        orderItems.forEach { order.addItem(it) }

        val payment =
            Payment(
                order = order,
                paymentIntentId = paymentResponse.id,
                amount = totalAmount,
                currency = req.currency.code,
            )

        order.assignPayment(payment)
        orderRepository.save(order)

        return OrderResponse(
            message = "Order placed successfully",
            paymentIntentId = paymentResponse.id,
        )
    }

    @Transactional
    fun getOrdersForMember(member: Member): List<OrderResponse> {
        val orders = orderRepository.findAllByMember(member)
        return orders.map {
            OrderResponse(
                message = "Order with ${it.items.size} item(s)",
                paymentIntentId = it.payment?.paymentIntentId ?: "N/A",
            )
        }
    }

    private fun createOrderItem(req: OrderItemRequest): OrderItem {
        val product =
            productRepository.findByIdOrNull(req.productId)
                ?: throw NotFoundException("Product not found: ${req.productId}")

        val option =
            optionRepository.findByIdOrNull(req.optionId)
                ?: throw NotFoundException("Option not found: ${req.optionId}")

        require(option.product?.id == product.id) { "Selected option does not belong to the specified product." }

        require(option.quantity >= req.quantity) { "Not enough stock available" }

        return OrderItem(
            product = product,
            option = option,
            quantity = req.quantity,
        )
    }

    private fun removeCartItem(
        member: Member,
        product: Product,
    ) {
        cartItemRepository.findByMemberAndProduct(member, product)
            .ifPresent { cartItemRepository.delete(it) }
    }

    private fun calculateAmount(
        price: Double,
        quantity: Int,
    ): Double {
        return price * quantity
    }
}
