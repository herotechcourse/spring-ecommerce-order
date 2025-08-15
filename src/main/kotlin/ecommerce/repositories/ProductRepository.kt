package ecommerce.repositories

import ecommerce.entities.ProductEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<ProductEntity, Long> {
    fun existsByName(name: String): Boolean
}
