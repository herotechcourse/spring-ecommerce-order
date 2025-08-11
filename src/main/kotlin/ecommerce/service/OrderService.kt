package ecommerce.service

import ecommerce.entity.Order
import ecommerce.repository.OrderJpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepo: OrderJpaRepository,
) {
    @Transactional(readOnly = true)
    fun findById(orderId: Long): Order {
        return orderRepo.findById(orderId).orElseThrow()
    }

    @Transactional
    fun create(order: Order): Order {
        return orderRepo.save(order)
    }
}
