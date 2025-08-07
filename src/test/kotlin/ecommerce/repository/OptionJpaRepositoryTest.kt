package ecommerce.repository

import ecommerce.entity.Option
import ecommerce.entity.Product
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class OptionJpaRepositoryTest {
    @Autowired
    private lateinit var optionJpaRepository: OptionJpaRepository

    @BeforeEach
    fun setUp() {
        val product =
            Product(
                name = "product",
                price = 0.2,
                imageUrl = "https://image.png",
                options = emptyList(),
            )

        val option =
            Option(
                name = "name",
                quantity = 1,
            )

        val productWithOption =
            Product(
                name = "product",
                price = 0.2,
                imageUrl = "https://image.png",
                options = listOf(option),
            )

        optionJpaRepository.save(option)
    }
}
