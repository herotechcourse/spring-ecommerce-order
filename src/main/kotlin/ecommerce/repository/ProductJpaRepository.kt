package ecommerce.repository

import ecommerce.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductJpaRepository : JpaRepository<Product, Long> {

    fun existsByName(name: String): Boolean

    fun existsByNameAndIdNot(name: String, id: Long): Boolean
}
