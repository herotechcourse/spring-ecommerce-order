package ecommerce.repository

import ecommerce.model.CartItem
import org.springframework.data.jpa.repository.JpaRepository

interface CartItemJpaRepository : JpaRepository<CartItem, Long>
