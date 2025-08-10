package ecommerce.service

import ecommerce.entity.Member
import ecommerce.entity.Option
import ecommerce.entity.Order
import ecommerce.entity.OrderItem
import ecommerce.entity.PaymentAttempt
import ecommerce.entity.Product
import ecommerce.entity.enumerated.OrderStatus
import ecommerce.entity.enumerated.PaymentAttemptStatus
import ecommerce.exception.InsufficientStockException
import ecommerce.repository.CartItemJpaRepository
import ecommerce.repository.OptionJpaRepository
import ecommerce.repository.OrderJpaRepository
import ecommerce.repository.PaymentAttemptJpaRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.mockk.verifyOrder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional

@ExtendWith(MockKExtension::class)
class OrderServiceTest {
    @MockK
    private lateinit var orderRepo: OrderJpaRepository

    @MockK
    private lateinit var optionRepo: OptionJpaRepository

    @MockK
    private lateinit var cartItemRepo: CartItemJpaRepository

    @MockK
    private lateinit var paymentAttemptRepo: PaymentAttemptJpaRepository

    @InjectMockKs
    private lateinit var service: OrderService

    @Test
    fun `createPending aggregates duplicates and calculates total`() {
        val m = member()
        val o1 = option(101, "opt-101", 999)
        val o2 = option(202, "opt-202", 999)
        val p1 = product(10, "P1", 100.0, o1)
        val p2 = product(20, "P2", 50.0, o2)

        val items = listOf(o1 to 1, o1 to 1, o2 to 1)

        every { orderRepo.save(any()) } answers { firstArg() }

        val order = service.createPending(m, items)

        assertThat(OrderStatus.PENDING).isEqualTo(order.status)
        assertThat(order.items.size).isEqualTo(2)
        assertThat(order.totalAmount).isEqualTo(250.0)

        val item1 = order.items.first { it.optionId == o1.id }
        assertThat(item1.priceSnapshot).isEqualTo(p1.price)
        assertThat(item1.productNameSnapshot).isEqualTo(p1.name)

        val item2 = order.items.first { it.optionId == o2.id }
        assertThat(item2.priceSnapshot).isEqualTo(p2.price)
        assertThat(item2.productNameSnapshot).isEqualTo(p2.name)
    }

    @Test
    fun `startPaymentAttempt creates pending attempt`() {
        val m = member()
        val order = Order(m)

        every { orderRepo.findById(1L) } returns Optional.of(order)
        every { paymentAttemptRepo.save(any()) } answers { firstArg() }

        val attempt = service.startPaymentAttempt(1L)

        assertThat(attempt.status).isEqualTo(PaymentAttemptStatus.PENDING)
        assertThat(attempt.order).isEqualTo(order)
    }

    @Test
    fun `finalizePaid happy path - decrements stock, cleans cart, marks PAID`() {
        val o1 = option(101, "opt-101", 999)
        val o2 = option(202, "opt-202", 999)
        val p1 = product(10, "P1", 100.0, o1)
        val p2 = product(20, "P2", 50.0, o2)

        val m = member()
        val order =
            Order(m).apply {
                items +=
                    OrderItem(
                        order = this,
                        productId = p1.id,
                        optionId = o1.id,
                        productNameSnapshot = p1.name,
                        priceSnapshot = p1.price,
                        quantity = 2,
                    )
                items +=
                    OrderItem(
                        order = this,
                        productId = p2.id,
                        optionId = o2.id,
                        productNameSnapshot = p2.name,
                        priceSnapshot = p2.price,
                        quantity = 1,
                    )
                totalAmount = 250.0
            }

        every { orderRepo.findById(1L) } returns Optional.of(order)
        every { optionRepo.decrementStockIfEnough(o1.id, 2) } returns 1
        every { optionRepo.decrementStockIfEnough(o2.id, 1) } returns 1
        every { cartItemRepo.deleteByCartMemberIdAndOptionId(m.id, o1.id) } returns 1
        every { cartItemRepo.deleteByCartMemberIdAndOptionId(m.id, o2.id) } returns 1
        every { paymentAttemptRepo.findAll() } returns listOf(PaymentAttempt(order))

        val updated = service.finalizePaid(1L, externalId = "123")

        assertThat(updated.status).isEqualTo(OrderStatus.PAID)
        verifyOrder {
            optionRepo.decrementStockIfEnough(o1.id, 2)
            optionRepo.decrementStockIfEnough(o2.id, 1)
        }
        verify(exactly = 1) { cartItemRepo.deleteByCartMemberIdAndOptionId(m.id, o1.id) }
        verify(exactly = 1) { cartItemRepo.deleteByCartMemberIdAndOptionId(m.id, o2.id) }
    }

    @Test
    fun `finalizePaid throws if any item lacks stock`() {
        val o = option(101, "opt-101", 4)
        val p = product(10, "P1", 100.0, o)

        val m = member()
        val order =
            Order(m).apply {
                items +=
                    OrderItem(
                        order = this,
                        productId = p.id,
                        optionId = o.id,
                        productNameSnapshot = p.name,
                        priceSnapshot = p.price,
                        quantity = 5,
                    )
                totalAmount = 500.0
            }

        every { orderRepo.findById(1L) } returns Optional.of(order)
        every { optionRepo.decrementStockIfEnough(o.id, 5) } returns 0

        assertThrows<InsufficientStockException> {
            service.finalizePaid(1L, externalId = null)
        }

        verify(exactly = 0) { cartItemRepo.deleteByCartMemberIdAndOptionId(any(), any()) }
    }

    @Test
    fun `markFailed marks order and attempt as REJECTED with reason`() {
        val m = member()
        val order = Order(m, id = 1L)
        val attempt = PaymentAttempt(order)

        every { orderRepo.findById(1L) } returns Optional.of(order)
        every { paymentAttemptRepo.findAll() } returns listOf(attempt)

        service.markFailed(1L, failureCode = "insufficient_funds", failureMessage = "Not enough money")

        assertThat(order.status).isEqualTo(OrderStatus.FAILED)
        assertThat(attempt.status).isEqualTo(PaymentAttemptStatus.REJECTED)
        assertThat(attempt.failureCode).isEqualTo("insufficient_funds")
        assertThat(attempt.failureMessage).isEqualTo("Not enough money")
    }

    private fun member(id: Long = 1L): Member = Member(email = "user@mail.com", password = "pw", id = 1L)

    private fun option(
        id: Long,
        name: String,
        qty: Int,
    ) = Option(name = name, quantity = qty, id = id)

    private fun product(
        id: Long,
        name: String,
        price: Double,
        vararg opts: Option,
    ) = Product(
        name,
        price,
        "https://img/$id.png",
        opts.toList(),
        id,
    )
}
