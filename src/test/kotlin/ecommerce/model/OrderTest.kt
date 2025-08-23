package ecommerce.model

import ecommerce.repository.OptionRepository
import ecommerce.repository.ProductRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderTest(
    @Autowired private val productRepository: ProductRepository,
    @Autowired private val optionRepository: OptionRepository,
) {
    @Test
    fun createOrder() {
        // given
        val member =
            Member(
                email = "test@test.com",
                password = "test",
            )
        val product = productRepository.findAll().first()
        val option = optionRepository.findById(1L).get()
        val orderItems =
            listOf(
                OrderItem(
                    member = member,
                    product = product,
                    option = option,
                ),
            )
        val currency = OrderCurrency.USD.name

        // when
        val order =
            Order(
                member = member,
                orderItems = orderItems,
                currency = currency,
            )

        // then
        assertThat(order.currency).isEqualTo(currency)
    }

    @Test
    fun `throw an exception if the order currency is invalid`() {
        // given
        val member =
            Member(
                email = "test@test.com",
                password = "test",
            )
        val product = productRepository.findAll().first()
        val option = optionRepository.findById(1L).get()
        val orderItems =
            listOf(
                OrderItem(
                    member = member,
                    product = product,
                    option = option,
                ),
            )
        val currency = "GBP"

        // when
        // then
        assertThrows<IllegalArgumentException> {
            Order(
                member = member,
                orderItems = orderItems,
                currency = currency,
            )
        }
    }
}
