package ecommerce.repository

import ecommerce.entity.Cart
import ecommerce.entity.Member
import ecommerce.entity.Option
import ecommerce.entity.Product
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime

@DataJpaTest
class CartPaginationTest
    @Autowired
    constructor(
        val cartRepository: CartJpaRepository,
        val productRepository: ProductJpaRepository,
        val memberRepository: MemberJpaRepository,
    ) {
        private lateinit var member: Member
        private lateinit var product: Product

        @BeforeEach
        fun setUp() {
            member =
                memberRepository.save(
                    Member(name = "Bob", email = "bob@example.com", password = "pw"),
                )

            val option =
                Option(
                    name = "PaginatedOption",
                    quantity = 1,
                )

            val baseProduct =
                Product(
                    name = "Product",
                    price = 12.34,
                    imageUrl = "http://image.com/paginated.png",
                    options = listOf(option),
                )

            product = productRepository.save(baseProduct)

            val now = LocalDateTime.now()
            cartRepository.save(Cart(member = member, product = product, createdAt = now))
        }

        @Test
        fun `findByMemberId returns paginated wishlist items`() {
            val page = cartRepository.findByMemberId(member.id, PageRequest.of(0, 10))
            assertThat(page.content).hasSize(1)
            assertThat(page.content[0].product.name).isEqualTo(product.name)
        }
    }
