package ecommerce.service

import ecommerce.repository.OptionJpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OptionService(
    private val optionRepo: OptionJpaRepository,
) {
    @Transactional
    fun decreaseQuantity(
        id: Long,
        quantity: Int,
    ) {
        require(quantity > 0) { "Quantity to decrease must be positive." }

        val updatedRows = optionRepo.decrementStockIfEnough(id, quantity)

        if (updatedRows == 0) {
            val currentQuantity = optionRepo.findById(id).map { it.quantity }.orElse(0)
            throw IllegalStateException("Not enough stock for option ID $id. Current quantity: $currentQuantity, requested: $quantity")
        }
    }

    @Transactional
    fun increaseQuantity(
        id: Long,
        quantity: Int,
    ) {
        require(quantity > 0) { "Quantity to increase must be positive." }

        val updatedRows = optionRepo.incrementStock(id, quantity)

        if (updatedRows == 0) {
            throw NoSuchElementException("Option with ID $id not found.")
        }
    }
}
