package ecommerce.service

import ecommerce.client.StripeClient
import ecommerce.dto.order.PlaceOrderRequest
import ecommerce.dto.order.PlaceOrderResponse
import ecommerce.dto.order.PurchasedItem
import ecommerce.exception.PaymentClientException
import ecommerce.exception.PaymentDeclinedException
import ecommerce.exception.PaymentServerException
import ecommerce.repository.CartJpaRepository
import ecommerce.repository.OptionJpaRepository
import ecommerce.repository.ProductJpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.roundToLong

@Service
class OrderService(
    private val stripeClient: StripeClient,
    private val productRepository: ProductJpaRepository,
    private val optionRepository: OptionJpaRepository,
    private val cartRepository: CartJpaRepository,
) {
    @Transactional
    private fun applySuccessfulOrderChanges(memberId: Long, optionId: Long, quantity: Int) {
        val option = optionRepository.findById(optionId)
            .orElseThrow { IllegalArgumentException("Option not found: $optionId") }

        option.subtract(quantity)
        optionRepository.save(option)

        cartRepository.deleteByMemberIdAndProductId(memberId, option.product!!.id)
    }

    fun placeOrder(
        memberId: Long,
        request: PlaceOrderRequest,
    ): PlaceOrderResponse {
        require(request.quantity > 0) { "Quantity must be greater than 0." }

        val product =
            productRepository.findById(request.productId)
                .orElseThrow { IllegalArgumentException("Product not found: ${request.productId}") }

        val option =
            optionRepository.findById(request.optionId)
                .orElseThrow { IllegalArgumentException("Option not found: ${request.optionId}") }

        if (option.product?.id != product.id) {
            throw IllegalArgumentException("Option ${option.id} does not belong to product ${product.id}.")
        }

        if (option.quantity < request.quantity) {
            throw IllegalArgumentException("Insufficient stock for option ${option.id}.")
        }

        val amountCents: Long = (product.price * 100.0 * request.quantity).roundToLong()
        val currency = "usd"

        return try {
            val payment = stripeClient.createPaymentIntent(amountCents, currency)

            if (payment.status.equals("requires_payment_method", ignoreCase = true) ||
                payment.lastPaymentError != null
            ) {
                val reason = payment.lastPaymentError?.code ?: "unknown_reason"
                val message = payment.lastPaymentError?.message ?: "Payment failed"
                throw PaymentDeclinedException("$reason: $message")
            }

            applySuccessfulOrderChanges(memberId, option.id, request.quantity)

            PlaceOrderResponse(
                orderStatus = payment.status.uppercase(),
                paymentIntentId = payment.id,
                amount = payment.amount,
                currency = payment.currency,
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
}
