package ecommerce.repository

import ecommerce.entity.Cart
import ecommerce.entity.Member
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.util.UUID

@DataJpaTest
class CartJpaRepositoryTest {
    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var cartJpaRepository: CartJpaRepository

    @Test
    fun `findByMemberId returns cart when exists`() {
        val (member, cart) = persistMemberWithCart()

        val found = cartJpaRepository.findByMemberId(member.id)

        assertThat(found).isNotNull
        assertThat(found!!.id).isEqualTo(cart.id)
        assertThat(found.member.id).isEqualTo(member.id)
    }

    @Test
    fun `findByMemberId returns null when cart missing for member`() {
        val member = Member(email = "no-cart-${UUID.randomUUID()}@example.com", password = "pwd")
        entityManager.persist(member)
        entityManager.flush()

        val found = cartJpaRepository.findByMemberId(member.id)

        assertThat(found).isNull()
    }

    @Test
    fun `findByMemberId pageable returns single element page when cart exists`() {
        val (_, cartA) = persistMemberWithCart("a-${UUID.randomUUID()}@example.com")
        val (memberB, cartB) = persistMemberWithCart("b-${UUID.randomUUID()}@example.com")
        val (_, cartC) = persistMemberWithCart("c-${UUID.randomUUID()}@example.com")

        val pageable: Pageable = PageRequest.of(0, 10)
        val page = cartJpaRepository.findByMemberId(memberB.id, pageable)

        assertThat(page.totalElements).isEqualTo(1L)
        assertThat(page.content).hasSize(1)
        assertThat(page.content[0].id).isEqualTo(cartB.id)
    }

    @Test
    fun `findByMemberId pageable returns empty page when not found`() {
        val pageable: Pageable = PageRequest.of(0, 5)
        val page = cartJpaRepository.findByMemberId(-9999L, pageable)

        assertThat(page.totalElements).isEqualTo(0L)
        assertThat(page.content).isEmpty()
    }

    private fun persistMemberWithCart(email: String = "user-${UUID.randomUUID()}@example.com"): Pair<Member, Cart> {
        val member = Member(email = email, password = "password")
        entityManager.persist(member)
        val cart = Cart(member = member)
        entityManager.persist(cart)
        entityManager.flush()
        return Pair(member, cart)
    }
}
