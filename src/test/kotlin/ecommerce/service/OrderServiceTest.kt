package ecommerce.service

import ecommerce.entity.Order
import ecommerce.repository.OrderJpaRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class OrderServiceTest {
    @MockK
    private lateinit var orderRepo: OrderJpaRepository

    @InjectMockKs
    private lateinit var orderService: OrderService

    @Test
    fun `should throw exception if order is not exist`() {
        every { orderRepo.findById(1L) } returns java.util.Optional.empty()
        assertThrows<NoSuchElementException> {
            orderService.findById(1L)
        }
    }

    @Test
    fun `should create and return order`() {
        val order = mockk<Order>()
        every { orderRepo.save(order) } returns order

        val result = orderService.create(order)

        assertThat(result).isEqualTo(order)
    }
}
