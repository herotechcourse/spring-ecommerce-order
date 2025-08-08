package ecommerce.repository

import ecommerce.entity.Option
import org.springframework.data.jpa.repository.JpaRepository

interface OptionJpaRepository : JpaRepository<Option, Long> {
    fun findByProductId(productId: Long): List<Option>

    fun existsByProductId(productId: Long): Boolean
}
