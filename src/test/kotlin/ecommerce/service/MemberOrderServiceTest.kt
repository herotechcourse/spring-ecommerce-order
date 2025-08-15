package ecommerce.service

import ecommerce.model.Cart
import ecommerce.model.CartProduct
import ecommerce.model.Option
import ecommerce.model.Product
import ecommerce.model.User
import ecommerce.repository.CartProductRepository
import ecommerce.repository.CartRepository
import ecommerce.repository.ProductRepository
import ecommerce.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.AfterTest

@SpringBootTest
@Transactional
class MemberOrderServiceTest {
    lateinit var member: User
    lateinit var cart: Cart
    lateinit var option: Option

    @Autowired
    private lateinit var cartProductRepository: CartProductRepository

    @Autowired
    private lateinit var cartRepository: CartRepository

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    lateinit var orderService: MemberOrderService

    @BeforeEach
    fun initBefore() {
        member =
            userRepository.save(
                User(
                    "order@test.com",
                    "123456789",
                    "test",
                ),
            )
        option =
            productRepository.save(
                Product(
                    "orderName",
                    "http://localhost:8080/image/upload/product1.jpg",
                    mutableListOf(
                        Option(
                            "name",
                            10.1,
                            51,
                            "http://localhost:8080/image/upload/product1.jpg",
                        ),
                    ),
                ),
            ).options.first()
        cart = cartRepository.save(Cart(member))
        cart.items.add(cartProductRepository.save(CartProduct(option, 10)))
        cartRepository.save(cart)
    }

    @AfterTest
    fun tearDown() {
        cartProductRepository.deleteAll()
        cartRepository.deleteAll()
        productRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun createCheckoutCartIntent() {
        val response = orderService.createCheckoutCartIntent(member.id)

        assertThat(response.orderId).isNotEqualTo(0L)
        assertThat(response.intentId).isNotEmpty
    }

    @Test
    fun confirmCheckout() {
        val intentResponse = orderService.createCheckoutCartIntent(member.id)
        val response = orderService.confirmCheckout(intentResponse.orderId)

        assertThat(response?.id).isEqualTo(intentResponse.intentId)
        assertThat(response?.status).isEqualTo("succeeded")

        val updatedCart = cartRepository.findById(cart.id).get()

        assertThat(updatedCart.items).isEmpty()
    }

    @Test
    fun getUserOrders() {
        val intentResponse = orderService.createCheckoutCartIntent(member.id)
        orderService.confirmCheckout(intentResponse.orderId)

        assertThat(orderService.getUserOrders(member.id)).isNotEmpty()
    }

    @Test
    fun getOrderById() {
        val intentResponse = orderService.createCheckoutCartIntent(member.id)

        assertThat(orderService.getOrderById(intentResponse.orderId).orderId).isEqualTo(intentResponse.orderId)
    }
}
