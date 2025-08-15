package ecommerce.config

import ecommerce.entities.CartItemEntity
import ecommerce.entities.MemberEntity
import ecommerce.entities.OptionEntity
import ecommerce.entities.ProductEntity
import ecommerce.repositories.CartItemRepository
import ecommerce.repositories.MemberRepository
import ecommerce.repositories.OptionRepository
import ecommerce.repositories.ProductRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime

@Configuration
class DatabaseConfig(
    private val productRepository: ProductRepository,
    private val memberRepository: MemberRepository,
    private val optionRepository: OptionRepository,
    private val cartItemRepository: CartItemRepository,
) {
    @Bean
    fun databaseInitializer(): CommandLineRunner =
        CommandLineRunner {
            val products =
                listOf(
                    ProductEntity(
                        name = "Car",
                        price = 1000.0,
                        imageUrl = "https://images.unsplash.com/photo-1494905998402-395d579af36f?w=400&h=400&fit=crop",
                    ),
                    ProductEntity(
                        name = "Bike",
                        price = 200.0,
                        imageUrl = "https://images.unsplash.com/photo-1571068316344-75bc76f77890?w=400&h=400&fit=crop",
                    ),
                    ProductEntity(
                        name = "Truck",
                        price = 30000.0,
                        imageUrl = "https://images.unsplash.com/photo-1586190848861-99aa4a171e90?w=400&h=400&fit=crop",
                    ),
                    ProductEntity(
                        name = "Laptop",
                        price = 1500.0,
                        imageUrl = "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400&h=400&fit=crop",
                    ),
                    ProductEntity(
                        name = "Phone",
                        price = 800.0,
                        imageUrl = "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=400&fit=crop",
                    ),
                ) +
                    (6..25).map { i ->
                        ProductEntity(
                            name = "Product $i",
                            price = i * 11.11,
                            imageUrl = "https://placeholder.vn/placeholder/400x400?bg=ff7f50&color=ffffff&text=Product$i",
                        )
                    }

            val savedProducts = productRepository.saveAll(products)

            val admin =
                MemberEntity(
                    name = "sebas",
                    email = "sebas@sebas.com",
                    password = "123456",
                    role = MemberEntity.Role.ADMIN,
                )

            val customers =
                (1..10).map { i ->
                    MemberEntity(
                        name = "User $i",
                        email = "user$i@example.com",
                        password = "pass",
                        role = MemberEntity.Role.CUSTOMER,
                    )
                }

            val savedMembers = memberRepository.saveAll(listOf(admin) + customers)

            val carOptions =
                listOf(
                    OptionEntity(name = "Red Color", quantity = 5, product = savedProducts[0], unitPrice = 1200.0),
                    OptionEntity(name = "Blue Color", quantity = 3, product = savedProducts[0], unitPrice = 1150.0),
                    OptionEntity(name = "Black Color", quantity = 4, product = savedProducts[0], unitPrice = 1250.0),
                    OptionEntity(name = "Automatic Transmission", quantity = 2, product = savedProducts[0], unitPrice = 1400.0),
                    OptionEntity(name = "Manual Transmission", quantity = 3, product = savedProducts[0], unitPrice = 1100.0),
                )

            val bikeOptions =
                listOf(
                    OptionEntity(name = "Small Size", quantity = 10, product = savedProducts[1], unitPrice = 220.0),
                    OptionEntity(name = "Medium Size", quantity = 15, product = savedProducts[1], unitPrice = 240.0),
                    OptionEntity(name = "Large Size", quantity = 8, product = savedProducts[1], unitPrice = 260.0),
                    OptionEntity(name = "Red Color", quantity = 12, product = savedProducts[1], unitPrice = 230.0),
                    OptionEntity(name = "Black Color", quantity = 7, product = savedProducts[1], unitPrice = 250.0),
                )

            val savedCarOptions = optionRepository.saveAll(carOptions)
            val savedBikeOptions = optionRepository.saveAll(bikeOptions)

            val cartItems =
                listOf(
                    CartItemEntity(
                        member = savedMembers[1],
                        option = savedCarOptions[0],
                        quantity = 1,
                        addedAt = LocalDateTime.now(),
                    ),
                    CartItemEntity(
                        member = savedMembers[1],
                        option = savedBikeOptions[1],
                        quantity = 2,
                        addedAt = LocalDateTime.now(),
                    ),
                )

            cartItemRepository.saveAll(cartItems)
        }
}
