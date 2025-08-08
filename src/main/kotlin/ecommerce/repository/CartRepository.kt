package ecommerce.repository

import ecommerce.model.Cart
import org.springframework.data.jpa.repository.JpaRepository

interface CartRepository : JpaRepository<Cart, Long>
