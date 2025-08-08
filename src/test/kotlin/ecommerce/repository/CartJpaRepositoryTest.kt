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
import java.time.LocalDateTime

@DataJpaTest
class CartJpaRepositoryTest
    @Autowired
    constructor(
        val cartRepository: CartJpaRepository,
        val productRepository: ProductJpaRepository,
        val memberRepository: MemberJpaRepository,
    ) {
        private lateinit var member: Member
        private lateinit var product: Product

        @BeforeEach
        fun setup() {
            member =
                memberRepository.save(
                    Member(name = "Alice", email = "alice@example.com", password = "pw"),
                )

            val baseProduct =
                Product(
                    name = "Widget",
                    price = 9.99,
                    imageUrl = "http://image.com/widget.png",
                    options = emptyList(),
                )
            val savedProduct = productRepository.save(baseProduct)

            val option =
                Option(
                    name = "Standard",
                    quantity = 1,
                    product = savedProduct,
                )
            val productWithOption =
                Product(
                    name = savedProduct.name,
                    price = savedProduct.price,
                    imageUrl = savedProduct.imageUrl,
                    options = listOf(option),
                    id = savedProduct.id,
                )

            product = productRepository.save(productWithOption)

            val now = LocalDateTime.now()
            cartRepository.save(Cart(member = member, product = product, createdAt = now))
        }

        @Test
        fun `findByMemberId should return items for given member`() {
            val items = cartRepository.findByMemberId(member.id)
            assertThat(items).hasSize(1)
            assertThat(items[0].member.id).isEqualTo(member.id)
        }

        @Test
        fun `deleteByMemberIdAndProductId should remove item`() {
            cartRepository.deleteByMemberIdAndProductId(member.id, product.id)
            val items = cartRepository.findByMemberId(member.id)
            assertThat(items).isEmpty()
        }
    }
