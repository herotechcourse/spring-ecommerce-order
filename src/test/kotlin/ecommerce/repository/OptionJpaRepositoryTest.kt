package ecommerce.repository

import ecommerce.entity.Option
import ecommerce.entity.Product
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import kotlin.test.Test

@DataJpaTest
class OptionJpaRepositoryTest {
    @Autowired
    private lateinit var optionJpaRepository: OptionJpaRepository

    @BeforeEach
    fun setUp() {
        val mockProduct =
            Product(
                "product",
                0.2,
                "https://image.png",
                listOf(
                    Option(
                        "name",
                        1,
                    ),
                ),
            )
    }

    @org.junit.jupiter.api.Test
    fun `findByProductId should return a list of options`() {
    }
}
