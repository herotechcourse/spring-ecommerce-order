package ecommerce.service

import ecommerce.dto.OrderItemRequest
import ecommerce.entity.Member
import ecommerce.entity.Option
import ecommerce.entity.Product
import ecommerce.entity.enumerated.OrderStatus
import ecommerce.repository.OrderJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException

@SpringBootTest
@Transactional
@AutoConfigureTestEntityManager
class OrderServiceIntegrationTest {
    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var orderRepo: OrderJpaRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    private lateinit var member: Member
    private lateinit var option1: Option
    private lateinit var option2: Option

    @BeforeEach
    fun setup() {
        member = Member(email = "test@member.com", password = "pw")
        entityManager.persist(member)

        option1 = Option(name = "M Size", quantity = 10)
        val product1 = Product(name = "T-shirt", price = 1000, "http://test.png", listOf(option1))
        entityManager.persist(product1)

        option2 = Option(name = "32 Width", quantity = 5)
        val product2 = Product(name = "Jeans", price = 2500, "http://test.png", listOf(option2))
        entityManager.persist(product2)

        entityManager.flush()
    }

    @Nested
    @DisplayName("Tests for order creation")
    inner class CreateOrderTests {
        @Test
        fun `order should created with valid products`() {
            val orderItems =
                listOf(
                    OrderItemRequest(optionId = option1.id, quantity = 2),
                    OrderItemRequest(optionId = option2.id, quantity = 1),
                )

            val createdOrder = orderService.create(member, orderItems)

            assertThat(createdOrder.id).isNotNull()
            assertThat(createdOrder.items).hasSize(2)
            assertThat(createdOrder.totalAmount).isEqualTo(4500)
            assertThat(createdOrder.status).isEqualTo(OrderStatus.PENDING)
            assertThat(createdOrder.member.id).isEqualTo(member.id)

            val foundOrder = orderRepo.findById(createdOrder.id).get()
            assertThat(foundOrder.totalAmount).isEqualTo(4500)
        }

        @Test
        fun `should throw IllegalArgumentException if cart is empty`() {
            val emptyOrderItems = emptyList<OrderItemRequest>()

            assertThrows<IllegalArgumentException> {
                orderService.create(member, emptyOrderItems)
            }
        }

        @Test
        fun `should throw NoSuchElementException during order creation with wrong optionId`() {
            val invalidOrderItems = listOf(OrderItemRequest(optionId = 999L, quantity = 1))

            assertThrows<NoSuchElementException> {
                orderService.create(member, invalidOrderItems)
            }
        }
    }

    @Nested
    @DisplayName("Test for find and update order")
    inner class FindAndUpdateOrderTests {
        private lateinit var existingOrder: ecommerce.entity.Order

        @BeforeEach
        fun createOrder() {
            existingOrder = ecommerce.entity.Order(member)
            existingOrder.totalAmount = 1000
            entityManager.persist(existingOrder)
            entityManager.flush()
        }

        @Test
        fun `findById should return order which exists`() {
            val foundOrder = orderService.findById(existingOrder.id)

            assertThat(foundOrder).isNotNull()
            assertThat(foundOrder.id).isEqualTo(existingOrder.id)
        }

        @Test
        fun `findById should throw NoSuchElementException if order doesn't exists`() {
            assertThrows<NoSuchElementException> {
                orderService.findById(999L)
            }
        }

        @Test
        fun `updateOrderStatus should update order status`() {
            val newStatus = OrderStatus.PAID

            orderService.updateOrderStatus(existingOrder.id, newStatus)
            entityManager.flush()
            entityManager.clear()

            val updatedOrder = orderRepo.findById(existingOrder.id).get()
            assertThat(updatedOrder.status).isEqualTo(newStatus)
        }

        @Test
        fun `updateOrderStatus should throw NoSuchElementException if order doesn't exists`() {
            assertThrows<NoSuchElementException> {
                orderService.updateOrderStatus(999L, OrderStatus.FAILED)
            }
        }
    }
}
