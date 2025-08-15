package ecommerce.repositories

import ecommerce.entities.MemberEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<MemberEntity, Long> {
    fun findByEmail(email: String): MemberEntity?

    fun save(member: MemberEntity): MemberEntity?

    fun existsByEmail(email: String): Boolean
}
