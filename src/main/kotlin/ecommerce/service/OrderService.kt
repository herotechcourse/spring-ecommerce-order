package ecommerce.service

import ecommerce.client.StripeClient
import ecommerce.dto.order.PlaceOrderRequest
import ecommerce.dto.order.PlaceOrderResponse
import ecommerce.dto.order.PurchasedItem
import ecommerce.entity.Member
import ecommerce.entity.Order
import ecommerce.entity.OrderItem
import ecommerce.entity.OrderStatus
import ecommerce.entity.Payment
import ecommerce.exception.PaymentClientException
import ecommerce.exception.PaymentDeclinedException
import ecommerce.exception.PaymentServerException
import ecommerce.repository.CartJpaRepository
import ecommerce.repository.MemberJpaRepository
import ecommerce.repository.OptionJpaRepository
import ecommerce.repository.OrderItemJpaRepository
import ecommerce.repository.OrderJpaRepository
import ecommerce.repository.PaymentJpaRepository
import ecommerce.repository.ProductJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.roundToLong

@Service
class OrderService(
    private val stripeClient: StripeClient,
    private val productRepository: ProductJpaRepository,
    private val optionRepository: OptionJpaRepository,
    private val cartRepository: CartJpaRepository,
    private val memberRepository: MemberJpaRepository,
    private val orderRepository: OrderJpaRepository,
    private val orderItemRepository: OrderItemJpaRepository,
    private val paymentRepository: PaymentJpaRepository,
) {
    fun placeOrder(
        memberId: Long,
        request: PlaceOrderRequest,
    ): PlaceOrderResponse {
        // 1) Validate input & load entities
        require(request.quantity > 0) { "Quantity must be greater than 0." }

        val product =
            productRepository.findByIdOrNull(request.productId)
                ?: throw IllegalArgumentException("Product not found: ${request.productId}")

        val option =
            optionRepository.findByIdOrNull(request.optionId)
                ?: throw IllegalArgumentException("Option not found: ${request.optionId}")

        if (option.product?.id != product.id) {
            throw IllegalArgumentException("Option ${option.id} does not belong to product ${product.id}.")
        }
        if (option.quantity < request.quantity) {
            throw IllegalArgumentException("Insufficient stock for option ${option.id}.")
        }

        // 2) Compute amount
        val amountCents: Long = (product.price * 100.0 * request.quantity).roundToLong()
        val currency = "usd"

        // 3) Call Stripe (outside transaction)
        return try {
            val paymentIntent = stripeClient.createPaymentIntent(amountCents, currency)


            if (paymentIntent.status.equals("requires_payment_method", ignoreCase = true) ||
                paymentIntent.lastPaymentError != null
            ) {
                val reason = paymentIntent.lastPaymentError?.code ?: "unknown_reason"
                val message = paymentIntent.lastPaymentError?.message ?: "Payment failed"
                throw PaymentDeclinedException("$reason: $message")
            }

            // 4) Persist Order + Items + Payment, decrement stock, clear cart (transactional)
            val persisted =
                persistAfterSuccessfulPayment(
                    memberId = memberId,
                    productId = product.id,
                    productName = product.name,
                    optionId = option.id,
                    optionName = option.name,
                    unitPrice = (product.price * 100.0).roundToLong(),
                    quantity = request.quantity,
                    paymentIntentId = paymentIntent.id,
                    paymentStatus = paymentIntent.status,
                    amount = paymentIntent.amount,
                    currency = paymentIntent.currency,
                )

            // 5) Build response
            PlaceOrderResponse(
                orderStatus = persisted.order.status.name,
                paymentIntentId = persisted.payment.intentId,
                amount = persisted.payment.amount,
                currency = persisted.payment.currency,
                items =
                    listOf(
                        PurchasedItem(
                            productId = product.id,
                            optionId = option.id,
                            quantity = request.quantity,
                        ),
                    ),
            )
        } catch (ex: PaymentDeclinedException) {
            throw ex
        } catch (ex: PaymentClientException) {
            throw ex
        } catch (ex: PaymentServerException) {
            throw ex
        }
    }

    @Transactional
    fun persistAfterSuccessfulPayment(
        memberId: Long,
        productId: Long,
        productName: String,
        optionId: Long,
        optionName: String,
        unitPrice: Long,
        quantity: Int,
        paymentIntentId: String,
        paymentStatus: String,
        amount: Long,
        currency: String,
    ): PersistedOrderBundle {
        val memberRef: Member = memberRepository.getReferenceById(memberId)

        // Create and save Order
        val order =
            orderRepository.save(
                Order(
                    member = memberRef,
                    status = OrderStatus.PAID,
                    totalAmount = amount,
                    currency = currency,
                ),
            )

        // Create and save OrderItem (snapshot fields)
        orderItemRepository.save(
            OrderItem(
                order = order,
                productId = productId,
                productName = productName,
                optionId = optionId,
                optionName = optionName,
                unitPrice = unitPrice,
                quantity = quantity,
            ),
        )

        // Save Payment snapshot
        val payment =
            paymentRepository.save(
                Payment(
                    order = order,
                    intentId = paymentIntentId,
                    status = paymentStatus,
                    amount = amount,
                    currency = currency,
                ),
            )

        // Decrement stock
        val option =
            optionRepository.findById(optionId)
                .orElseThrow { IllegalArgumentException("Option not found: $optionId") }
        option.subtract(quantity)
        optionRepository.save(option)

        // Clear purchased option from cart
        cartRepository.deleteByMemberIdAndOptionId(memberId, optionId)

        return PersistedOrderBundle(order, payment)
    }

    data class PersistedOrderBundle(
        val order: Order,
        val payment: Payment,
    )
}
