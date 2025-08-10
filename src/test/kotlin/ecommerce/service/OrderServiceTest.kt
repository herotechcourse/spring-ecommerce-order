package ecommerce.service

import ecommerce.dto.OrderPlacementRequest
import ecommerce.dto.RegisteredMember
import ecommerce.dto.Role
import ecommerce.exception.BadRequestException
import ecommerce.exception.NotFoundException
import ecommerce.model.Cart
import ecommerce.model.Member
import ecommerce.model.Option
import ecommerce.model.Order
import ecommerce.model.OrderStatus
import ecommerce.model.Product
import ecommerce.repository.CartRepository
import ecommerce.repository.MemberRepository
import ecommerce.repository.OptionRepository
import ecommerce.repository.OrderRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class OrderServiceTest {
    private lateinit var orderService: OrderService

    @Mock
    lateinit var orderRepository: OrderRepository

    @Mock
    lateinit var paymentService: PaymentService

    @Mock
    lateinit var optionRepository: OptionRepository

    @Mock
    lateinit var cartRepository: CartRepository

    @Mock
    lateinit var memberRepository: MemberRepository

    @BeforeEach
    fun setUp() {
        orderService =
            OrderService(
                orderRepository,
                paymentService,
                optionRepository,
                cartRepository,
                memberRepository,
            )
    }

    @Test
    fun `test payment success with valid request and stock`() {
        val req = OrderPlacementRequest(productOptionId = 1L, quantity = 1, paymentMethod = "card")
        val member = RegisteredMember(id = 10L, email = "test@example.com", Role.USER)

        val option =
            mock(Option::class.java).apply {
                `when`(isAvailable(1)).thenReturn(true)
                `when`(product).thenReturn(Product(name = "Product A", price = 100.0, "https://"))
                `when`(name).thenReturn("Test Option")
            }
        val savedOrder =
            Order(
                id = 99L,
                member = mock(Member::class.java),
                createdAt = LocalDateTime.now(),
                status = OrderStatus.PENDING,
                paymentAmount = 100.0,
                currency = "usd",
                paymentMethod = "pm_visa_card",
                checkoutSessionId = "session123",
            )

        `when`(optionRepository.findById(1L)).thenReturn(Optional.of(option))
        `when`(memberRepository.findById(member.id)).thenReturn(Optional.of(mock(Member::class.java)))
        `when`(cartRepository.findCartByMemberId(member.id)).thenReturn(mock(Cart::class.java))
        `when`(orderRepository.save(any())).thenReturn(savedOrder)

        val response = orderService.placeOrder(req, member)

        assertEquals("SUCCESS", response.status)
        assertEquals(99L, response.orderId)
        verify(paymentService).processPayment(any())
    }

    @Test
    fun `test unavailable stock throws BadRequestException`() {
        val req = OrderPlacementRequest(productOptionId = 1L, quantity = 5, paymentMethod = "card")
        val member = RegisteredMember(id = 10L, email = "test@example.com", Role.USER)

        val option =
            mock(Option::class.java).apply {
                `when`(isAvailable(5)).thenReturn(false)
            }

        `when`(optionRepository.findById(1L)).thenReturn(Optional.of(option))

        assertThrows<BadRequestException> {
            orderService.placeOrder(req, member)
        }
        verify(paymentService, never()).createPaymentIntent(any())
    }

    @Test
    fun `test payment failure throws same exception and order status is FAILED`() {
        val req = OrderPlacementRequest(productOptionId = 1L, quantity = 1, paymentMethod = "card")
        val member = RegisteredMember(id = 10L, email = "test@example.com", Role.USER)

        val option =
            mock(Option::class.java).apply {
                `when`(isAvailable(1)).thenReturn(true)
            }
        val savedOrder =
            Order(
                id = 99L,
                member = mock(Member::class.java),
                createdAt = LocalDateTime.now(),
                status = OrderStatus.FAILED,
                paymentAmount = 100.0,
                currency = "usd",
                paymentMethod = "pm_visa_card",
                checkoutSessionId = "session123",
            )
        `when`(optionRepository.findById(1L)).thenReturn(Optional.of(option))

        val ex =
            assertThrows<RuntimeException> {
                orderService.placeOrder(req, member)
            }
        assertEquals(OrderStatus.FAILED, savedOrder.status)
    }

    @Test
    fun `test member not found throws NotFoundException`() {
        val req = OrderPlacementRequest(productOptionId = 1L, quantity = 1, paymentMethod = "card")
        val member = RegisteredMember(id = 10L, email = "test@example.com", Role.USER)

        val option =
            mock(Option::class.java).apply {
                `when`(isAvailable(1)).thenReturn(true)
            }

        `when`(optionRepository.findById(1L)).thenReturn(Optional.of(option))

        assertThrows<NotFoundException> {
            orderService.placeOrder(req, member)
        }
        verify(paymentService, never()).processPayment(any())
    }

    @Test
    fun `test cart does not contain ordered item still success`() {
        val req = OrderPlacementRequest(productOptionId = 1L, quantity = 1, paymentMethod = "card")
        val member = RegisteredMember(id = 10L, email = "test@example.com", Role.USER)

        val option =
            mock(Option::class.java).apply {
                `when`(isAvailable(1)).thenReturn(true)
                `when`(product).thenReturn(Product(name = "Product A", price = 100.0, "https://"))
                `when`(name).thenReturn("Test Option")
            }
        val savedOrder =
            Order(
                id = 99L,
                member = mock(Member::class.java),
                createdAt = LocalDateTime.now(),
                status = OrderStatus.PENDING,
                paymentAmount = 100.0,
                currency = "usd",
                paymentMethod = "pm_visa_card",
                checkoutSessionId = "session123",
            )

        `when`(optionRepository.findById(1L)).thenReturn(Optional.of(option))
        `when`(memberRepository.findById(member.id)).thenReturn(Optional.of(mock(Member::class.java)))
        `when`(cartRepository.findCartByMemberId(member.id)).thenReturn(null) // No cart
        `when`(orderRepository.save(any())).thenReturn(savedOrder)

        val response = orderService.placeOrder(req, member)

        assertEquals("SUCCESS", response.status)
        assertEquals(99L, response.orderId)
        verify(cartRepository).findCartByMemberId(member.id)
    }
}
