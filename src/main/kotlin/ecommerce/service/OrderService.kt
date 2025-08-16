package ecommerce.service

import ecommerce.dto.options.OptionQuantityDto
import ecommerce.dto.order.OrderResponseDto
import ecommerce.dto.order.PlaceOrderRequestDto
import ecommerce.dto.order.PlaceOrderResponseDto
import ecommerce.dto.stripe.PaymentRequestDto
import ecommerce.dto.stripe.PaymentResponseDto
import ecommerce.exception.PaymentException
import ecommerce.exception.StripePaymentException
import ecommerce.extensions.OrderStateMapper
import ecommerce.extensions.toDto
import ecommerce.model.CartProduct
import ecommerce.model.User
import ecommerce.repository.CartProductRepository
import ecommerce.repository.OptionRepository
import ecommerce.repository.OrderRepository
import ecommerce.repository.UserRepository
import ecommerce.stripe.StripeClient
import ecommerce.stripe.toUserFriendlyMessage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val userRepository: UserRepository,
    private val optionRepository: OptionRepository,
    private val cartProductRepository: CartProductRepository,
    private val orderRepository: OrderRepository,
    private val stripeClient: StripeClient,
) {
    @Transactional
    fun placeOrder(
        userId: Long,
        placeOrderRequestDto: PlaceOrderRequestDto,
    ): PlaceOrderResponseDto {
        require(userId > 0) { "Invalid user ID: $userId" }
        require(placeOrderRequestDto.currency.isNotBlank()) { "Currency must be provided" }
        require(placeOrderRequestDto.paymentMethodId.isNotBlank()) { "Payment method must be provided" }

        val user = loadUserById(userId)
        val cartProducts = loadCartProductsForUser(user)
        val optionQuantityList = validateStockAndPrepareLineItems(cartProducts)
        val totalAmount = calculateTotalAmount(optionQuantityList)

        require(totalAmount > 0) { "Order total must be positive" }

        var order = OrderStateMapper.newPending(user, totalAmount, placeOrderRequestDto)
        order = orderRepository.save(order)

        val payment: PaymentResponseDto =
            try {
                createStripeCheckoutSession(totalAmount, placeOrderRequestDto)
            } catch (ex: StripePaymentException) {
                val raw = ex.declineCode ?: ex.code ?: "payment_error"
                val message = raw.toUserFriendlyMessage()
                order = OrderStateMapper.applyFailed(order, message)
                orderRepository.save(order)
                throw PaymentException(message)
            } catch (ex: Exception) {
                val message = "Unable to process the payment: ${ex.message ?: "technical error"}"
                order = OrderStateMapper.applyFailed(order, message)
                orderRepository.save(order)
                throw PaymentException(message)
            }

        if (payment.status != "succeeded") {
            val raw = payment.declineCode ?: payment.status
            val message = raw.toUserFriendlyMessage()
            order = OrderStateMapper.applyFailed(order, message, payment.id)
            orderRepository.save(order)
            throw PaymentException(message)
        }

        deductStockAndClearCart(user, optionQuantityList)

        order = OrderStateMapper.applyPaid(order, payment.id, optionQuantityList)
        order = orderRepository.save(order)

        return PlaceOrderResponseDto(order.id, order.stripeSessionId)
    }

    @Transactional(readOnly = true)
    fun listOrders(userId: Long): List<OrderResponseDto> {
        val user = loadUserById(userId)
        return orderRepository.findAllByUserOrderByCreatedAtDesc(user)
            .map { it.toDto() }
    }

    private fun loadUserById(userId: Long) =
        userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Invalid user ID: $userId") }

    private fun loadCartProductsForUser(user: User): List<CartProduct> {
        val cart = checkNotNull(user.cart) { "Cart not found for user ${user.id}" }
        val cartProducts = cartProductRepository.findByCart(cart)
        require(cartProducts.isNotEmpty()) { "Cart is empty for user ${user.id}" }
        return cartProducts
    }

    private fun validateStockAndPrepareLineItems(cartProducts: List<CartProduct>): List<OptionQuantityDto> {
        return cartProducts.map { cartProduct ->
            val productOption = optionRepository.findById(cartProduct.option.id).orElse(null)
            requireNotNull(productOption) { "Invalid option ID: ${cartProduct.option.id}" }
            require(productOption.quantity >= cartProduct.quantity) {
                "Insufficient stock for option ID: ${productOption.id}"
            }
            OptionQuantityDto(productOption, cartProduct.quantity)
        }
    }

    private fun calculateTotalAmount(optionQuantityList: List<OptionQuantityDto>): Double {
        return optionQuantityList.sumOf { (productOption, quantity) ->
            productOption.price * quantity
        }
    }

    private fun createStripeCheckoutSession(
        totalAmount: Double,
        placeOrderRequestDto: PlaceOrderRequestDto,
    ): PaymentResponseDto {
        return stripeClient.createCheckoutSession(
            PaymentRequestDto(
                totalAmount,
                placeOrderRequestDto.currency,
                placeOrderRequestDto.paymentMethodId,
            ),
        )
    }

    private fun deductStockAndClearCart(
        user: User,
        optionQuantityList: List<OptionQuantityDto>,
    ) {
        optionQuantityList.forEach { (productOption, quantity) ->
            productOption.quantity -= quantity
            check(productOption.quantity >= 0) { "Negative stock for option ID: ${productOption.id}" }
            optionRepository.save(productOption)
            cartProductRepository.deleteByCartAndOption(checkNotNull(user.cart), productOption)
        }
    }
}
