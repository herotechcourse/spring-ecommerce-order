package ecommerce.repository

import ecommerce.model.CartHistory
import org.springframework.data.jpa.repository.JpaRepository

interface CartHistoryJpaRepository : JpaRepository<CartHistory, Long>
