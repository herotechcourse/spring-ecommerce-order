package ecommerce.repository

import ecommerce.model.Product

interface ProductStore {
    fun countProducts(): Int

    fun findAll(): List<Product>

    fun findById(id: Long): Product?

    fun findByName(name: String): Product?

    fun existsByName(name: String): Boolean

    fun save(product: Product): Product

    fun update(
        id: Long,
        product: Product,
    ): Boolean

    fun delete(id: Long): Boolean
}
