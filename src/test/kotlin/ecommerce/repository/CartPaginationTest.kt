package ecommerce.repository

import ecommerce.entity.Cart
import ecommerce.entity.Member
import ecommerce.entity.Product
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@DataJpaTest
class CartPaginationTest {

    @Autowired
    lateinit var cartJpaRepository: CartJpaRepository

    @Autowired
    lateinit var memberJpaRepository: MemberJpaRepository

    @Autowired
    lateinit var productJpaRepository: ProductJpaRepository

    private lateinit var member: Member

    @BeforeEach
    fun setUp() {
        member = memberJpaRepository.save(Member(name = "TestUser", email = "user@test.com", password = "secret"))

        val products = (1..12).map {
            productJpaRepository.save(
                Product(
                    name = "Product $it",
                    price = 10.0 + it,
                    imageUrl = "http://test.com/product$it.png"
                )
            )
        }

        products.forEach {
            cartJpaRepository.save(Cart(member = member, product = it))
        }
    }

    @Test
    fun `findByMemberId returns paginated wishlist items`() {
        val pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id"))
        val page = cartJpaRepository.findByMemberId(member.id, pageable)

        assertThat(page.content).hasSize(5)
        assertThat(page.totalElements).isEqualTo(12)
        assertThat(page.totalPages).isEqualTo(3)
    }

}