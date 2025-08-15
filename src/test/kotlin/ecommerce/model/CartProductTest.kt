package ecommerce.model

import ecommerce.repository.CartProductRepository
import ecommerce.repository.CartRepository
import ecommerce.repository.ProductRepository
import ecommerce.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CartProductTest {
    lateinit var cartProduct: CartProduct
    lateinit var options: List<Option>

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var cartRepository: CartRepository

    @Autowired
    private lateinit var cartProductRepository: CartProductRepository

    @BeforeEach
    fun initBeforeEachTest() {
        val member = userRepository.save(User("user@testing.com", "user1234", "user"))
        val cart = cartRepository.save(Cart(member))
        options =
            productRepository.save(
                Product(
                    "product",
                    "http://localhost:8080/image/upload/product1.jpg",
                    mutableListOf(
                        Option(
                            "Option 1",
                            15.0,
                            10,
                            "https://example.com/test.png",
                        ),
                    ),
                ),
            ).options
        cart.addProduct(options[0], 10)
        cartProduct = cart.items.first()
    }

    @AfterEach
    fun afterEachTest() {
        cartProductRepository.deleteAll()
        productRepository.deleteAll()
        cartRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun incrementQuantity() {
        cartProduct.incrementQuantity()
        assertThat(cartProduct.quantity).isEqualTo(11)
    }

    @Test
    fun decrementQuantity() {
        cartProduct.decrementQuantity()
        assertThat(cartProduct.quantity).isEqualTo(9)
    }
}
