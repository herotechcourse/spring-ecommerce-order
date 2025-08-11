package ecommerce.repository

import ecommerce.dto.Role
import ecommerce.model.Member
import ecommerce.model.Order
import ecommerce.model.OrderItem
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class OrderRepositoryTest {
    @Autowired
    lateinit var orderRepository: OrderRepository

    @Autowired
    lateinit var orderItemRepository: OrderItemRepository

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Test
    fun `order should contain order item`() {
        val member = memberRepository.save(Member("member@mail.com", "name", "password", Role.USER.name))
        val order = orderRepository.save(Order(member = member, paymentAmount = 1000, paymentMethod = "pm_card_visa"))
        val orderId = order.id ?: throw Exception("order id is null")
        val orderItem = OrderItem(2, "washing machine", "M1203")
        order.addItem(orderItem)
        orderRepository.flush()
        val actual = orderRepository.findById(orderId).get()
        assertThat(actual.items[0].productName == orderItem.productName)
    }

    @Test
    fun `order item should be saved when order is saved`() {
        val member = memberRepository.save(Member("member@mail.com", "name", "password", Role.USER.name))
        val order = orderRepository.save(Order(member = member, paymentAmount = 1000, paymentMethod = "pm_card_visa"))
        val orderItem = OrderItem(2, "washing machine", "M1203")
        order.addItem(orderItem)
        orderRepository.save(order)
        val items = orderItemRepository.findAll()
        assertThat(items).hasSize(1)
    }
}
