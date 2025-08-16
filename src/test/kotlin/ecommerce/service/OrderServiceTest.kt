package ecommerce.service

import ecommerce.dto.order.PlaceOrderRequestDto
import ecommerce.dto.stripe.PaymentRequestDto
import ecommerce.dto.stripe.PaymentResponseDto
import ecommerce.enums.OrderStatus
import ecommerce.enums.UserRole
import ecommerce.exception.PaymentException
import ecommerce.exception.StripePaymentException
import ecommerce.model.Cart
import ecommerce.model.CartProduct
import ecommerce.model.Option
import ecommerce.model.Order
import ecommerce.model.User
import ecommerce.repository.CartProductRepository
import ecommerce.repository.OptionRepository
import ecommerce.repository.OrderRepository
import ecommerce.repository.UserRepository
import ecommerce.stripe.StripeClient
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockKExtension::class)
class OrderServiceTest {
    @MockK lateinit var userRepository: UserRepository

    @MockK lateinit var cartProductRepository: CartProductRepository

    @MockK lateinit var optionRepository: OptionRepository

    @MockK lateinit var orderRepository: OrderRepository

    @MockK lateinit var stripeClient: StripeClient

    private lateinit var orderService: OrderService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        optionRepository = mockk()
        cartProductRepository = mockk()
        orderRepository = mockk()
        stripeClient = mockk()
        orderService =
            OrderService(
                userRepository,
                optionRepository,
                cartProductRepository,
                orderRepository,
                stripeClient,
            )
    }

    @Test
    fun `placeOrder - process successfully`() {
        val userId = 1L
        val cart = Cart(id = 10L)
        val user =
            User(
                "ann@example.com",
                "password12345",
                "User",
                UserRole.USER,
                cart,
                1L,
            )

        val option = Option("Option", 50.0, 5, "https://example.com/img.png")
        val cartProduct = CartProduct(cart, option, 2)
        val totalAmount = option.price * cartProduct.quantity

        val paymentResp =
            PaymentResponseDto(
                "pi_123",
                totalAmount.toLong(),
                "usd",
                "succeeded",
                null,
            )

        val request =
            PlaceOrderRequestDto(
                "usd",
                "pm_card_visa",
            )

        val savedOrder =
            Order(
                user,
                paymentResp.id,
                totalAmount,
                request.currency,
                request.paymentMethodId,
                OrderStatus.PAID,
                null,
                LocalDateTime.now(),
                mutableListOf(),
                99L,
            )

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { cartProductRepository.findByCart(cart) } returns listOf(cartProduct)
        every { optionRepository.findById(option.id) } returns Optional.of(option)
        every { stripeClient.createCheckoutSession(any<PaymentRequestDto>()) } returns paymentResp
        every { optionRepository.save(option) } returns option
        every { cartProductRepository.deleteByCartAndOption(cart, option) } just runs

        every { orderRepository.save(any()) } answers { firstArg() }
        every { orderRepository.save(match { it.status == OrderStatus.PAID }) } returns savedOrder

        val response = orderService.placeOrder(userId, request)

        assertEquals(99L, response.orderId)
        assertEquals("pi_123", response.checkoutSession)
        assertEquals(3, option.quantity) // 5 - 2

        verify(exactly = 1) { optionRepository.save(option) }
        verify(exactly = 1) { cartProductRepository.deleteByCartAndOption(cart, option) }
        verify(exactly = 2) { orderRepository.save(any<Order>()) } // PENDING + PAID
    }

    @Test
    fun `placeOrder - Stripe error should throw PaymentException and not touch stock or cart`() {
        val userId = 1L
        val cart = Cart(id = 10L)
        val user = User("ann@example.com", "password1234", "User", UserRole.USER, cart, 1L)
        val option = Option("Opt", 30.0, 4, "https://example.com/img.png")
        val cartProduct = CartProduct(cart, option, 2)

        val request =
            PlaceOrderRequestDto(
                currency = "usd",
                paymentMethodId = "pm_card_visa_chargeDeclinedInsufficientFunds",
            )

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { cartProductRepository.findByCart(cart) } returns listOf(cartProduct)
        every { optionRepository.findById(option.id) } returns Optional.of(option)

        every { orderRepository.save(any()) } answers { firstArg() }

        every { stripeClient.createCheckoutSession(any<PaymentRequestDto>()) } throws
            StripePaymentException(
                "card_declined",
                code = "insufficient_funds",
                declineCode = "insufficient_funds",
            )

        val ex =
            assertThrows<PaymentException> {
                orderService.placeOrder(userId, request)
            }
        assertTrue(ex.message!!.contains("insufficient", ignoreCase = true))

        assertEquals(4, option.quantity)
        verify(exactly = 0) { optionRepository.save(any()) }
        verify(exactly = 0) { cartProductRepository.deleteByCartAndOption(any(), any()) }

        verify(exactly = 2) { orderRepository.save(any<Order>()) }
    }

    @Test
    fun `placeOrder - insufficient stock should throw IllegalArgumentException and not call downstream methods`() {
        val userId = 1L
        val cart = Cart(id = 10L)
        val user = User("ann@example.com", "password1234", "User", UserRole.USER, cart, 1L)
        val option = Option("Opt", 30.0, 1, "https://example.com/img.png")
        val cartProduct = CartProduct(cart, option, 2)
        val request = PlaceOrderRequestDto(currency = "usd", paymentMethodId = "pm_card_visa")

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { cartProductRepository.findByCart(cart) } returns listOf(cartProduct)
        every { optionRepository.findById(option.id) } returns Optional.of(option)

        val exception =
            assertThrows<IllegalArgumentException> {
                orderService.placeOrder(userId, request)
            }
        assertTrue(exception.message!!.contains("Insufficient stock for option ID: ${option.id}"))

        verify(exactly = 0) { orderRepository.save(any()) }
        verify(exactly = 0) { stripeClient.createCheckoutSession(any()) }
        verify(exactly = 0) { optionRepository.save(any()) }
        verify(exactly = 0) { cartProductRepository.deleteByCartAndOption(any(), any()) }
    }

    @Test
    fun `placeOrder - empty cart should throw IllegalArgumentException and not call downstream methods`() {
        val userId = 1L
        val cart = Cart(id = 20L)
        val user = User("ann@example.com", "password1234", "User", UserRole.USER, cart, 1L)

        val request = PlaceOrderRequestDto(currency = "usd", paymentMethodId = "pm_card_visa")

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { cartProductRepository.findByCart(cart) } returns emptyList()

        val exception =
            assertThrows<IllegalArgumentException> {
                orderService.placeOrder(userId, request)
            }
        assertTrue(exception.message!!.contains("Cart is empty for user ${user.id}"))

        verify(exactly = 0) { optionRepository.findById(any()) }
        verify(exactly = 0) { stripeClient.createCheckoutSession(any()) }
        verify(exactly = 0) { optionRepository.save(any()) }
        verify(exactly = 0) { cartProductRepository.deleteByCartAndOption(any(), any()) }
        verify(exactly = 0) { orderRepository.save(any()) }
    }

    @Test
    fun `placeOrder - invalid userId should throw IllegalArgumentException and not call downstream methods`() {
        val userId = 999L
        val request = PlaceOrderRequestDto(currency = "usd", paymentMethodId = "pm_card_visa")

        every { userRepository.findById(userId) } returns Optional.empty()

        val exception =
            assertThrows<IllegalArgumentException> {
                orderService.placeOrder(userId, request)
            }
        assertEquals("Invalid user ID: $userId", exception.message)

        verify(exactly = 0) { cartProductRepository.findByCart(any()) }
        verify(exactly = 0) { optionRepository.findById(any()) }
        verify(exactly = 0) { stripeClient.createCheckoutSession(any()) }
        verify(exactly = 0) { optionRepository.save(any()) }
        verify(exactly = 0) { cartProductRepository.deleteByCartAndOption(any(), any()) }
        verify(exactly = 0) { orderRepository.save(any()) }
    }

    @Test
    fun `placeOrder - missing option should throw IllegalArgumentException and not call downstream methods`() {
        val userId = 1L
        val cart = Cart(id = 30L)
        val user = User("ann@example.com", "password", "User", UserRole.USER, cart, 1L)
        val option = Option("OptX", 20.0, 3, "https://example.com/img.png")
        val cartProduct = CartProduct(cart, option, 1)
        val request = PlaceOrderRequestDto(currency = "usd", paymentMethodId = "pm_card_visa")

        every { userRepository.findById(userId) } returns Optional.of(user)
        every { cartProductRepository.findByCart(cart) } returns listOf(cartProduct)
        every { optionRepository.findById(option.id) } returns Optional.empty()

        val ex =
            assertThrows<IllegalArgumentException> {
                orderService.placeOrder(userId, request)
            }
        assertEquals("Invalid option ID: ${option.id}", ex.message)

        verify(exactly = 0) { stripeClient.createCheckoutSession(any()) }
        verify(exactly = 0) { optionRepository.save(any()) }
        verify(exactly = 0) { cartProductRepository.deleteByCartAndOption(any(), any()) }
        verify(exactly = 0) { orderRepository.save(any()) }
    }
}
