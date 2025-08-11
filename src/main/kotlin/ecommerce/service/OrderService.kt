package ecommerce.service

import ecommerce.entity.Order
import ecommerce.entity.OrderItem
import ecommerce.entity.Payment
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
) {
    @Transactional
    fun createOrder(
        memberId: Long,
        productOptionId: Long,
        amount: Int,
    ) {
        val cart =
            cartRepository.findByMemberId(memberId)
                ?: throw NoSuchElementException("Cart not found")

        val cartItem =
            cartItemRepository.findByCartIdAndProductOptionId(cart.id!!, productOptionId)
                ?: throw NoSuchElementException("Cart item not found")

        val product =
            cartItem.product
                ?: throw IllegalStateException("Cart item product is null")

        val member =
            memberRepository.findById(memberId)
                .orElseThrow { NoSuchElementException("Member not found") }

        val totalPrice = product.price * amount.toDouble()
        val amountInCents = (totalPrice * 100).toLong() // Stripe needs smallest currency unit

        val order =
            Order(
                member = member,
                status = "PENDING",
            )
        orderRepository.save(order)

        val orderItem =
            OrderItem(
                order = order,
                product = product,
                productOption = cartItem.productOption,
                quantity = amount,
            )
        orderItemRepository.save(orderItem)

        val paymentIntentId =
            stripeClientService.createPaymentIntent(
                amount = amountInCents,
                currency = "usd",
            )

        val payment =
            Payment(
                order = order,
                status = "PENDING",
                stripePaymentIntentId = paymentIntentId.id,
                amount = amountInCents,
            )
        paymentRepository.save(payment)

        cart.cartItems.remove(cartItem)
        cartRepository.save(cart)
    }

    @Transactional
    fun markOrderAsPaid(paymentIntentId: String) {
        val payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
            ?: throw NoSuchElementException("Payment not found for paymentIntentId $paymentIntentId")

        payment.status = "PAID"
        paymentRepository.save(payment)

        val order = payment.order ?: throw IllegalStateException("Order for payment is null")

        if(payment.status == "PAID") {
            order.status = "PAID"
        }
        orderRepository.save(order)
    }
}
