package ecommerce.repository

import ecommerce.entity.CartEntity
import ecommerce.entity.MemberEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
internal class CartRepositoryJpaTest
    @Autowired
    constructor(
        private val cartRepository: CartRepositoryJpa,
        private val memberRepository: MemberRepositoryJpa,
    ) {
        @Test
        fun `findByMemberId should return a cart`() {
            val member =
                MemberEntity(
                    email = "alice@example.com",
                    password = "secret",
                    role = "USER",
                    name = "Alice",
                )
            val savedMember = memberRepository.save(member)

            val cart =
                CartEntity(
                    id = null,
                    memberId = savedMember.id!!,
                )
            cartRepository.save(cart)

            val foundCart = cartRepository.findByMemberId(savedMember.id!!)

            assertThat(foundCart).isNotNull
            assertThat(foundCart?.memberId).isEqualTo(savedMember.id)
        }
    }
