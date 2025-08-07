package ecommerce.repository

import ecommerce.model.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.Optional

interface ProductRepository : JpaRepository<Product, Long>, PagingAndSortingRepository<Product, Long> {
    fun findByName(name: String): Optional<Product>
}
