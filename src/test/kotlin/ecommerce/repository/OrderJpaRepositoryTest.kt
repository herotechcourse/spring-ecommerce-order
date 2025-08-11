package ecommerce.repository

import ecommerce.entity.Member
import ecommerce.entity.Option
import ecommerce.entity.Order
import ecommerce.entity.OrderItem
import ecommerce.entity.Product
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class OrderJpaRepositoryTest {
    @Autowired
    private lateinit var orderRepo: OrderJpaRepository

    @Autowired
    private lateinit var memberRepo: MemberJpaRepository

    @Autowired
    private lateinit var orderItemRepo: OrderItemJpaRepository

    @Test
    fun `should save and retrieve order`() {
        val saved = orderRepo.save(order())
        val found = orderRepo.findById(saved.id)
        assertThat(found).isPresent
        assertThat(found.get().id).isEqualTo(saved.id)
        assertThat(found.get().member.email).isEqualTo("u@u")
        assertThat(found.get().items).hasSize(1)
    }

    @Test
    fun `should update updatedAt when touched`() {
        val order = orderRepo.save(order())
        val oldUpdatedAt = order.updatedAt

        Thread.sleep(500)

        order.touch()
        val updated = orderRepo.save(order)

        assertThat(updated.updatedAt).isAfter(oldUpdatedAt)
    }

    @Test
    fun `should cascade persist order items`() {
        val saved = orderRepo.save(order())

        val reloaded = orderRepo.findById(saved.id)
        assertThat(reloaded).isPresent
        assertThat(reloaded.get().items).hasSize(1)

        val item = reloaded.get().items.first()
        assertThat(item.order.id).isEqualTo(saved.id)
        assertThat(item.productNameSnapshot.value).isEqualTo("product")
    }

    @Test
    fun `should delete order and cascade delete order items`() {
        val saved = orderRepo.save(order())
        val orderId = saved.id

        assertThat(orderItemRepo.findAll()).hasSize(1)

        orderRepo.deleteById(orderId)

        assertThat(orderRepo.findById(orderId)).isEmpty()

        assertThat(orderItemRepo.findAll()).isEmpty()
    }

    private fun order(): Order {
        val options = listOf(
            Option("1", 1, id = OPTION_ID),
        )
        val product = Product("product", 7, "http://t.org", options, id = PRODUCT_ID)
        val member = memberRepo.save(Member("u@u", "pw"))
        return Order(member).let {
            it.items += options.map { option ->
                OrderItem(
                    it,
                    PRODUCT_ID,
                    OPTION_ID,
                    product.name,
                    product.price,
                    1,
                )
            }
            it
        }
    }

    companion object {
        const val PRODUCT_ID = 1L
        const val OPTION_ID = 1L
    }
}
