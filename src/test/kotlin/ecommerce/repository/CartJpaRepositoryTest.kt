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
        private lateinit var cartSaved: Cart

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
                    options =
                        listOf(
                            Option(
                                name = "Standard",
                                quantity = 1,
                            ),
                        ),
                )

            product = productRepository.save(baseProduct)

            val cart = Cart(member)
            cart.add(product.options.first(), 1)
            cartSaved = cartRepository.save(cart)
        }

        @Test
        fun `findByMemberId should return items for given member`() {
            val items = cartRepository.findByMemberId(member.id)
            assertThat(items).hasSize(1)
            assertThat(items[0].member.id).isEqualTo(member.id)
        }

        @Test
        fun `findByMemberIdAndId should return the cart for given member and id`() {
            val found = cartRepository.findByMemberIdAndId(member.id, cartSaved.id)
            assertThat(found).isNotNull
            assertThat(found!!.id).isEqualTo(cartSaved.id)
            assertThat(found.member.id).isEqualTo(member.id)
        }

        @Test
        fun `findByMemberId with pageable should return a page`() {
            val page = cartRepository.findByMemberId(member.id, PageRequest.of(0, 10))
            assertThat(page.totalElements).isEqualTo(1)
            assertThat(page.content).hasSize(1)
            assertThat(page.content[0].id).isEqualTo(cartSaved.id)
        }
    }
