package ecommerce.repository

import ecommerce.exception.ElementNotFoundException
import ecommerce.model.Member
import org.springframework.data.jpa.repository.JpaRepository

fun MemberJpaRepository.getByIdOrThrow(id: Long): Member =
    this.findById(id)
        .orElseThrow { ElementNotFoundException("Member with ID $id not found") }

interface MemberJpaRepository : JpaRepository<Member, Long> {
    fun findByEmail(email: String): Member?

    fun existsByEmail(email: String): Boolean
}
