package ecommerce.repository

import ecommerce.entity.Product
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@DataJpaTest
class ProductPaginationTest {
    @Autowired
    lateinit var productJpaRepository: ProductJpaRepository

    @BeforeEach
    fun setUp() {
        val products =
            (1..10).map {
                Product(
                    name = "Product $it",
                    price = 10.0 + it,
                    imageUrl = "http://test.com/product$it.png",
                )
            }
        productJpaRepository.saveAll(products)
    }

    @Test
    fun `findAll returns paginated and sorted results`() {
        val pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "name"))
        val page = productJpaRepository.findAll(pageable)

        assertThat(page.content).hasSize(5)
        assertThat(page.totalElements).isEqualTo(10)
        assertThat(page.totalPages).isEqualTo(2)
        assertThat(page.content[0].name).isEqualTo("Product 9")
    }
}
