package ecommerce.repository

import ecommerce.entity.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberJpaRepository : JpaRepository<Member, Long> {
    fun findByEmail(email: String): Member?

    fun findByEmailAndName(
        email: String,
        name: String,
    ): Member?
}
