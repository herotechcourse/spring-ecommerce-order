package ecommerce.service

import ecommerce.dto.CreateOrderResponse
import ecommerce.dto.MemberResponse
import ecommerce.entity.Order
import ecommerce.entity.OrderItem
import ecommerce.entity.Payment
import ecommerce.enums.OrderAndPaymentStatus
import ecommerce.handler.PaymentFailedException
import ecommerce.handler.StripeConnectionException
import ecommerce.handler.StripePaymentFailedException
import ecommerce.repository.CartItemRepositoryJpa
import ecommerce.repository.CartRepositoryJpa
import ecommerce.repository.MemberRepositoryJpa
import ecommerce.repository.OrderItemRepository
import ecommerce.repository.OrderRepository
import ecommerce.repository.PaymentRepository
import ecommerce.service.payment.StripeClientService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val cartRepository: CartRepositoryJpa,
    private val cartItemRepository: CartItemRepositoryJpa,
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val paymentRepository: PaymentRepository,
    private val memberRepository: MemberRepositoryJpa,
    private val stripeClientService: StripeClientService,
    private val orderTransactionService: OrderTransactionService,
) {
    @Transactional
    fun createOrder(
        memberId: Long,
        productOptionId: Long,
        quantity: Int,
        paymentMethod: String,
        currency: String,
    ): CreateOrderResponse {
        val cart =
            cartRepository.findByMemberId(memberId)
                ?: throw NoSuchElementException("Cart not found")

        val cartItem =
            cartItemRepository.findByCartIdAndProductOptionId(cart.id!!, productOptionId)
                ?: throw NoSuchElementException("Cart item not found")

        val product = cartItem.product

        val member =
            memberRepository.findById(memberId)
                .orElseThrow { NoSuchElementException("Member not found") }

        val totalPrice = product.price * quantity.toDouble()
        val amountInCents = (totalPrice * 100).toLong() // Stripe requires cents

        val order =
            orderRepository.save(
                Order(
                    member = member,
                    status = OrderAndPaymentStatus.PENDING,
                ),
            )

        orderItemRepository.save(
            OrderItem(
                order = order,
                product = product,
                productOption = cartItem.productOption,
                quantity = quantity,
            ),
        )

        val paymentIntentId =
            try {
                stripeClientService.createPaymentIntent(
                    amountInCents,
                    currency,
                    paymentMethod,
                )
            } catch (e: StripePaymentFailedException) {
                throw PaymentFailedException("Payment failed: ${e.message}")
            } catch (e: StripeConnectionException) {
                throw PaymentFailedException("Payment connection failed: ${e.message}")
            }

        paymentRepository.save(
            Payment(
                order = order,
                status = OrderAndPaymentStatus.PENDING,
                stripePaymentIntentId = paymentIntentId,
                amount = amountInCents,
            ),
        )

        cartRepository.save(cart)

        return CreateOrderResponse(
            orderId = order.id!!,
            paymentIntentId = paymentIntentId,
        )
    }

    fun confirmPayment(
        orderId: Long,
        member: MemberResponse,
    ): String {
        val stripePaymentIntentId = orderTransactionService.preparePaymentProcessing(orderId, member.id)

        return try {
            stripeClientService.confirmPaymentIntent(stripePaymentIntentId)
            orderTransactionService.completeSuccessfulPayment(stripePaymentIntentId)
            "Payment successful, order $orderId marked as PAID"
        } catch (e: StripePaymentFailedException) {
            orderTransactionService.markPaymentAsFailed(stripePaymentIntentId, "Payment failed: ${e.stripeErrorCode ?: "Unknown code"}")
            throw PaymentFailedException("Payment was declined: ${e.message ?: "Unknown error"}")
        } catch (e: StripeConnectionException) {
            orderTransactionService.markPaymentAsFailed(stripePaymentIntentId, "Network error: ${e.message}")
            throw PaymentFailedException("Temporary payment issue. Please try again.")
        }
    }
}
