package ecommerce.repository
import ecommerce.entity.MemberEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepositoryJpa : JpaRepository<MemberEntity, Long> {
    fun existsByEmail(email: String): Boolean

    fun findByEmail(email: String): MemberEntity?
}
