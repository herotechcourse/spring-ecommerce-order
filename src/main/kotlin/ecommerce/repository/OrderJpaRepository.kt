package ecommerce.repository

import ecommerce.exception.ElementNotFoundException
import ecommerce.model.Order
import org.springframework.data.jpa.repository.JpaRepository

fun OrderJpaRepository.findByIdOrThrow(id: Long): Order =
    this.findById(id)
        .orElseThrow { ElementNotFoundException("Order with ID $id not found") }

interface OrderJpaRepository : JpaRepository<Order, Long>
