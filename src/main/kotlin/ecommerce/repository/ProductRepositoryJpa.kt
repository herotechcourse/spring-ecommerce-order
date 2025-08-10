package ecommerce.repository

import ecommerce.entity.ProductEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepositoryJpa : JpaRepository<ProductEntity, Long> {
    fun findByName(name: String): ProductEntity?

    fun existsByName(name: String): Boolean

    fun findAllByPrice(
        price: Double,
        pageable: Pageable,
    ): Page<ProductEntity>

    override fun findAll(pageable: Pageable): Page<ProductEntity>
}
