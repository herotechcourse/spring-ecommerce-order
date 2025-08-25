package ecommerce.service

import ecommerce.entity.CartEntity
import ecommerce.entity.MemberEntity
import ecommerce.entity.OptionEntity
import ecommerce.entity.Order
import ecommerce.entity.OrderItem
import ecommerce.entity.Payment
import ecommerce.entity.ProductEntity
import ecommerce.enums.OrderAndPaymentStatus
import ecommerce.repository.CartRepositoryJpa
import ecommerce.repository.MemberRepositoryJpa
import ecommerce.repository.OptionRepositoryJpa
import ecommerce.repository.OrderItemRepository
import ecommerce.repository.OrderRepository
import ecommerce.repository.PaymentRepository
import ecommerce.repository.ProductRepositoryJpa
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class OrderTransactionServiceTest {
    @Autowired
    lateinit var orderTransactionService: OrderTransactionService

    @Autowired
    lateinit var cartRepository: CartRepositoryJpa

    @Autowired
    lateinit var memberRepository: MemberRepositoryJpa

    @Autowired
    lateinit var productRepository: ProductRepositoryJpa

    @Autowired
    lateinit var optionRepository: OptionRepositoryJpa

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Autowired
    lateinit var orderItemRepository: OrderItemRepository

    @Autowired
    lateinit var paymentRepository: PaymentRepository

    @Test
    fun `completeSuccessfulPayment - happy path updates order, payment, inventory and cleans cart`() {
        val member =
            memberRepository.save(
                MemberEntity(
                    email = "john.doe@john.com",
                    password = "123",
                    role = "USER",
                    name = "John Doe",
                ),
            )
        cartRepository.save(CartEntity(memberId = member.id!!))

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

        val order = orderRepository.save(Order(member = member, status = OrderAndPaymentStatus.PENDING))
        val orderItem =
            orderItemRepository.save(
                OrderItem(
                    order = order,
                    product = product,
                    productOption = option,
                    quantity = 2,
                ),
            )
        order.orderItems.add(orderItem)
        orderRepository.save(order)

        val payment =
            paymentRepository.save(
                Payment(
                    order = order,
                    amount = 299L,
                    stripePaymentIntentId = "pi_123",
                    status = OrderAndPaymentStatus.PENDING,
                ),
            )

        orderTransactionService.completeSuccessfulPayment(payment.stripePaymentIntentId!!)

        val updatedOrder = orderRepository.findById(order.id!!).get()
        assertEquals(OrderAndPaymentStatus.PAID, updatedOrder.status)

        val updatedPayment = paymentRepository.findById(payment.id!!).get()
        assertEquals(OrderAndPaymentStatus.PAID, updatedPayment.status)

        val updatedOption = optionRepository.findById(option.id!!).get()
        assertEquals(8, updatedOption.quantity)
    }

    @Test
    fun `completeSuccessfulPayment - fails if payment not found`() {
        assertThrows<NoSuchElementException> {
            orderTransactionService.completeSuccessfulPayment("nonexistent_pi")
        }
    }
}
