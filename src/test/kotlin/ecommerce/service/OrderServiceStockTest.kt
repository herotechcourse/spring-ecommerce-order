package ecommerce.service

import ecommerce.model.Cart
import ecommerce.model.CartItem
import ecommerce.model.Currency
import ecommerce.model.Member
import ecommerce.model.Order
import ecommerce.model.OrderItem
import ecommerce.model.OrderStatus
import ecommerce.model.PaymentStatus
import ecommerce.model.Product
import ecommerce.model.ProductOption
import ecommerce.model.Role
import ecommerce.repository.CartItemRepository
import ecommerce.repository.CartRepository
import ecommerce.repository.MemberRepository
import ecommerce.repository.OrderRepository
import ecommerce.repository.ProductOptionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class OrderServiceStockTest {
    @Mock
    private lateinit var orderRepository: OrderRepository

    @Mock
    private lateinit var memberRepository: MemberRepository

    @Mock
    private lateinit var productOptionRepository: ProductOptionRepository

    @Mock
    private lateinit var cartItemRepository: CartItemRepository

    @Mock
    private lateinit var cartRepository: CartRepository

    private lateinit var orderService: OrderService
    private lateinit var member: Member
    private lateinit var product: Product
    private lateinit var productOption: ProductOption
    private lateinit var cart: Cart
    private lateinit var cartItem: CartItem
    private lateinit var order: Order

    @BeforeEach
    fun setUp() {
        orderService =
            OrderService(
                orderRepository,
                memberRepository,
                productOptionRepository,
                cartItemRepository,
                cartRepository,
            )

        member = Member("test@test.com", "password", "Test User", Role.USER, id = 1L)
        product = Product("Test Product", 29.99, 10, "test.jpg", id = 1L)
        productOption = ProductOption("Size M", 10, product, 29.99, id = 1L)
        cart = Cart(member, id = 1L)
        cartItem = CartItem(cart, productOption, 3, id = 1L)

        order = Order(member, currency = Currency.EUR, id = 1L)
        val orderItem = OrderItem.fromProductOption(productOption, 3, order)
        order.addOrderItem(orderItem)
        order.orderStatus = OrderStatus.PENDING
        order.paymentStatus = PaymentStatus.PENDING
    }

    @Test
    fun `should decrease product stock when payment confirmed`() {
        // Given
        val initialStock = 10
        val orderedQuantity = 3
        assertThat(productOption.quantity).isEqualTo(initialStock)

        `when`(orderRepository.findById(1L)).thenReturn(Optional.of(order))
        `when`(orderRepository.save(any())).thenReturn(order)
        `when`(cartRepository.findByMemberId(1L)).thenReturn(cart)

        // When
        orderService.confirmOrderPayment(1L)

        // Then
        assertThat(productOption.quantity).isEqualTo(initialStock - orderedQuantity)
        verify(productOptionRepository).save(productOption)
    }

    @Test
    fun `should remove cart items when fully ordered`() {
        // Given
        `when`(orderRepository.findById(1L)).thenReturn(Optional.of(order))
        `when`(orderRepository.save(any())).thenReturn(order)
        `when`(cartRepository.findByMemberId(1L)).thenReturn(cart)
        `when`(cartItemRepository.findByCartIdAndProductOptionId(1L, 1L)).thenReturn(listOf(cartItem))

        // When
        orderService.confirmOrderPayment(1L)

        // Then
        verify(cartItemRepository).delete(cartItem)
    }

    @Test
    fun `should partially decrease cart items when cart quantity greater than ordered`() {
        // Given
        val cartItemWithHighQuantity = CartItem(cart, productOption, 5, id = 1L)
        val orderWithLowQuantity = Order(member, currency = Currency.EUR, id = 1L)
        val orderItem = OrderItem.fromProductOption(productOption, 2, orderWithLowQuantity)
        orderWithLowQuantity.addOrderItem(orderItem)

        `when`(orderRepository.findById(1L)).thenReturn(Optional.of(orderWithLowQuantity))
        `when`(orderRepository.save(any())).thenReturn(orderWithLowQuantity)
        `when`(cartRepository.findByMemberId(1L)).thenReturn(cart)
        `when`(cartItemRepository.findByCartIdAndProductOptionId(1L, 1L)).thenReturn(listOf(cartItemWithHighQuantity))

        // When
        orderService.confirmOrderPayment(1L)

        // Then
        assertThat(cartItemWithHighQuantity.quantity).isEqualTo(3) // 5 - 2 = 3
        verify(cartItemRepository).save(cartItemWithHighQuantity)
    }
}
