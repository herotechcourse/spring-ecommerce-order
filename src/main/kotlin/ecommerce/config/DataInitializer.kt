package ecommerce.config

import ecommerce.enums.UserRole
import ecommerce.model.Cart
import ecommerce.model.Option
import ecommerce.model.Product
import ecommerce.model.User
import ecommerce.repository.CartRepository
import ecommerce.repository.ProductRepository
import ecommerce.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val cartRepository: CartRepository,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        println("DataInitializer is running!")
        if (userRepository.count() == 0L) {
            userRepository.save(
                User(
                    "admin@test.com",
                    "admin123",
                    "admin",
                    UserRole.ADMIN,
                ),
            )
            val user =
                userRepository.save(
                    User(
                        "user@test.com",
                        "user123",
                        "user",
                    ),
                )
            cartRepository.save(Cart(user))
        }
        if (productRepository.count() == 0L) {
            productRepository.saveAll(createProducts())
        }
    }

    private fun createProducts(): List<Product> {
        return (1..10).map { i ->
            Product(
                "Product-${i + 1}",
                "https://cdn.pixabay.com/photo/2018/01/14/23/12/nature-3082832_1280.jpg",
                (0..3).map { j ->
                    Option(
                        "Option-${i + 1}-${j + 1}",
                        10.0,
                        10,
                        "https://cdn.pixabay.com/photo/2018/01/14/23/12/nature-3082832_1280.jpg",
                    )
                }.toMutableList(),
            )
        }
    }
}
