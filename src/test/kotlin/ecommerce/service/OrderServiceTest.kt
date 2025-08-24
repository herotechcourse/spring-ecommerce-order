package ecommerce.service

import ecommerce.dto.MemberResponse
import ecommerce.entity.CartEntity
import ecommerce.entity.CartItemEntity
import ecommerce.entity.MemberEntity
import ecommerce.entity.OptionEntity
import ecommerce.entity.Order
import ecommerce.entity.Payment
import ecommerce.entity.ProductEntity
import ecommerce.enums.OrderAndPaymentStatus
import ecommerce.handler.PaymentFailedException
import ecommerce.handler.StripePaymentFailedException
import ecommerce.repository.CartItemRepositoryJpa
import ecommerce.repository.CartRepositoryJpa
import ecommerce.repository.MemberRepositoryJpa
import ecommerce.repository.OptionRepositoryJpa
import ecommerce.repository.OrderRepository
import ecommerce.repository.PaymentRepository
import ecommerce.repository.ProductRepositoryJpa
import ecommerce.service.payment.StripeClientService
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // H2 in-memory
@Transactional
class OrderServiceTest {
    @Autowired
    lateinit var orderService: OrderService

    @Autowired
    lateinit var cartRepository: CartRepositoryJpa

    @Autowired
    lateinit var cartItemRepository: CartItemRepositoryJpa

    @Autowired
    lateinit var memberRepository: MemberRepositoryJpa

    @Autowired
    lateinit var productRepository: ProductRepositoryJpa

    @Autowired
    lateinit var optionRepository: OptionRepositoryJpa

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Autowired
    lateinit var paymentRepository: PaymentRepository

    @Autowired
    lateinit var stripeClientService: StripeClientService // injected mock

    @TestConfiguration
    class TestConfig {
        @Bean
        fun stripeClientService(): StripeClientService = mock(StripeClientService::class.java)
    }

    @BeforeEach
    fun resetMocks() {
        reset(stripeClientService)
    }

    @Test
    fun `createOrder - happy path (integration)`() {
        val member =
            memberRepository.save(
                MemberEntity(
                    email = "john.doe@john.com",
                    password = "123456",
                    role = "user",
                    name = "John Doe",
                ),
            )
        val cart = cartRepository.save(CartEntity(memberId = member.id!!))
        memberRepository.save(member)

        val option = OptionEntity(name = "defaultOption", quantity = 10)
        var product =
            ProductEntity(
                name = "Test product",
                price = 2.99,
                imageUrl = "https://img.jpg",
                options = mutableListOf(option),
            )
        option.product = product
        product = productRepository.save(product)

        optionRepository.save(option)
        val cartItem = cartItemRepository.save(CartItemEntity(cart, product, option, quantity = 1))
        cart.cartItems.add(cartItem)
        cartRepository.save(cart)

        val paymentIntentId = "pi_123"

        whenever(stripeClientService.createPaymentIntent(299L, "usd", "pm_card_visa"))
            .thenReturn(paymentIntentId)

        val response = orderService.createOrder(member.id!!, option.id!!, 1, "pm_card_visa", "usd")

        val savedOrder = orderRepository.findById(response.orderId).get()
        assertEquals(member.id, savedOrder.member!!.id)
        assertEquals(paymentIntentId, response.paymentIntentId)

        val savedPayment = paymentRepository.findAll().first()
        assertEquals(paymentIntentId, savedPayment.stripePaymentIntentId)
        assertEquals(299L, savedPayment.amount)
    }

    @Test
    fun `createOrder - cart not found`() {
        val member =
            memberRepository.save(
                MemberEntity(email = "john.doe@john.com", password = "123456", role = "user", name = "John Doe"),
            )

        val exception =
            assertThrows<NoSuchElementException> {
                orderService.createOrder(member.id!!, 999L, 1, "pm_card_visa", "usd")
            }

        assertTrue(exception.message!!.contains("Cart not found"))
    }

    @Test
    fun `createOrder - cart item not found`() {
        val member =
            memberRepository.save(
                MemberEntity(email = "john.doe@john.com", password = "123456", role = "user", name = "John Doe"),
            )
        cartRepository.save(CartEntity(memberId = member.id!!))

        val option = OptionEntity(name = "defaultOption", quantity = 10)
        val product =
            ProductEntity(
                name = "Test product",
                price = 2.99,
                imageUrl = "https://img.jpg",
                options = mutableListOf(option),
            )
        option.product = product
        productRepository.save(product)
        optionRepository.save(option)

        val exception =
            assertThrows<NoSuchElementException> {
                orderService.createOrder(member.id!!, option.id!!, 1, "pm_card_visa", "usd")
            }

        assertTrue(exception.message!!.contains("Cart item not found"))
    }

    @Test
    fun `createOrder - payment fails`() {
        val member =
            memberRepository.save(
                MemberEntity(
                    email = "john.doe@john.com",
                    password = "123456",
                    role = "user",
                    name = "John Doe",
                ),
            )
        val cart = cartRepository.save(CartEntity(memberId = member.id!!))
        memberRepository.save(member)

        val option = OptionEntity(name = "defaultOption", quantity = 10)
        var product =
            ProductEntity(
                name = "Test product",
                price = 2.99,
                imageUrl = "https://img.jpg",
                options = mutableListOf(option),
            )
        option.product = product
        product = productRepository.save(product)
        optionRepository.save(option)

        val cartItem = cartItemRepository.save(CartItemEntity(cart, product, option, quantity = 1))
        cart.cartItems.add(cartItem)
        cartRepository.save(cart)

        whenever(stripeClientService.createPaymentIntent(299L, "usd", "pm_card_visa"))
            .thenThrow(StripePaymentFailedException("Stripe API error: card_declined", "card_declined"))

        val exception =
            assertThrows<PaymentFailedException> {
                orderService.createOrder(member.id!!, option.id!!, 1, "pm_card_visa", "usd")
            }

        assertTrue(exception.message!!.contains("Payment failed"))
    }

    @Test
    fun `processPayment - happy path`() {
        val memberEntity =
            memberRepository.save(
                MemberEntity(email = "john.doe@john.com", password = "123", role = "user", name = "John Doe"),
            )

        cartRepository.save(CartEntity(memberId = memberEntity.id!!))

        val order =
            orderRepository.save(
                Order(member = memberEntity, status = OrderAndPaymentStatus.PENDING),
            )
        val payment =
            paymentRepository.save(
                Payment(order = order, status = OrderAndPaymentStatus.PENDING, stripePaymentIntentId = "pi_123", amount = 299L),
            )
        order.payment = payment
        orderRepository.save(order)

        whenever(stripeClientService.confirmPaymentIntent("pi_123")).thenReturn("pi_123")

        val memberResponse =
            MemberResponse(
                id = memberEntity.id!!,
                email = memberEntity.email,
                name = "John",
                role = "USER",
            )

        val result = orderService.confirmPayment(order.id!!, memberResponse)

        assertTrue(result.contains("Payment successful"))
        val updatedOrder = orderRepository.findById(order.id!!).get()
        assertEquals(OrderAndPaymentStatus.PAID, updatedOrder.status)
        assertEquals(OrderAndPaymentStatus.PAID, updatedOrder.payment!!.status)
    }

    @Test
    fun `processPayment - order not found`() {
        val memberResponse =
            MemberResponse(
                id = 1,
                email = "a@b.com",
                name = "John",
                role = "USER",
            )

        val exception =
            assertThrows<NoSuchElementException> {
                orderService.confirmPayment(999, memberResponse)
            }
        assertTrue(exception.message!!.contains("Order not found"))
    }

    @Test
    fun `processPayment - payment missing`() {
        val memberEntity =
            memberRepository.save(
                MemberEntity(email = "john.doe@john.com", password = "123", role = "user", name = "John Doe"),
            )

        val order =
            orderRepository.save(
                Order(member = memberEntity, status = OrderAndPaymentStatus.PENDING),
            )

        val memberResponse =
            MemberResponse(
                id = memberEntity.id!!,
                email = memberEntity.email,
                name = "John",
                role = "USER",
            )

        val exception =
            assertThrows<NoSuchElementException> {
                orderService.confirmPayment(order.id!!, memberResponse)
            }

        assertTrue(exception.message!!.contains("Payment not found"))
    }

    @Test
    fun `processPayment - payment already made`() {
        val memberEntity =
            memberRepository.save(
                MemberEntity(email = "john.doe@john.com", password = "123", role = "user", name = "John Doe"),
            )

        val order =
            orderRepository.save(
                Order(member = memberEntity, status = OrderAndPaymentStatus.PAID),
            )
        val payment =
            paymentRepository.save(
                Payment(order = order, status = OrderAndPaymentStatus.PAID, stripePaymentIntentId = "pi_123", amount = 299L),
            )
        order.payment = payment
        orderRepository.save(order)

        val memberResponse =
            MemberResponse(
                id = memberEntity.id!!,
                email = memberEntity.email,
                name = "John",
                role = "USER",
            )

        val exception =
            assertThrows<PaymentFailedException> {
                orderService.confirmPayment(order.id!!, memberResponse)
            }

        assertTrue(exception.message!!.contains("Payment already made"))
    }

    @Test
    fun `processPayment - stripe fails`() {
        val memberEntity =
            memberRepository.save(
                MemberEntity(email = "john.doe@john.com", password = "123", role = "user", name = "John Doe"),
            )

        val order =
            orderRepository.save(
                Order(member = memberEntity, status = OrderAndPaymentStatus.PENDING),
            )
        val payment =
            paymentRepository.save(
                Payment(
                    order = order,
                    status = OrderAndPaymentStatus.PENDING,
                    stripePaymentIntentId = "pi_123",
                    amount = 299L,
                ),
            )
        order.payment = payment
        orderRepository.save(order)

        doThrow(StripePaymentFailedException("Card declined", "card_declined"))
            .whenever(stripeClientService).confirmPaymentIntent("pi_123")

        val memberResponse =
            MemberResponse(
                id = memberEntity.id!!,
                email = memberEntity.email,
                name = "John",
                role = "ADMIN",
            )

        val exception =
            assertThrows<PaymentFailedException> {
                orderService.confirmPayment(order.id!!, memberResponse)
            }

        assertTrue(exception.message!!.contains("Payment was declined"))

        val updatedPayment = paymentRepository.findByStripePaymentIntentId("pi_123")
        assertEquals(OrderAndPaymentStatus.FAILED, updatedPayment?.status)
    }
}
