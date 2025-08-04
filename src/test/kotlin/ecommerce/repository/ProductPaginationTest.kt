package ecommerce.repository

import ecommerce.entity.Option
import ecommerce.entity.Product
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@DataJpaTest
class ProductPaginationTest(
    @Autowired val productRepository: ProductJpaRepository,
    @Autowired val optionRepository: OptionJpaRepository,
) {

    @BeforeEach
    fun setUp() {
        (1..10).forEach {
            val baseProduct = Product(
                name = "Product $it",
                price = 10.0 + it,
                imageUrl = "http://test.com/product$it.png",
                options = emptyList(),
            )
            val savedProduct = productRepository.save(baseProduct)

            val option = Option("Option $it", 1, savedProduct)
            val productWithOption = Product(
                name = savedProduct.name,
                price = savedProduct.price,
                imageUrl = savedProduct.imageUrl,
                options = listOf(option),
                id = savedProduct.id,
            )

            productRepository.save(productWithOption)
        }
    }

    @Test
    fun `findAll returns paginated and sorted results`() {
        val pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"))
        val page = productRepository.findAll(pageRequest)

        assertThat(page.content).hasSize(5)
        assertThat(page.totalElements).isEqualTo(10)
        assertThat(page.content[0].id).isGreaterThan(page.content[4].id)
    }
}