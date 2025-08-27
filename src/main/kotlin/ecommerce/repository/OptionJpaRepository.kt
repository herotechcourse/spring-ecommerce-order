package ecommerce.repository

import ecommerce.exception.ElementNotFoundException
import ecommerce.model.Option
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull

fun OptionJpaRepository.getByIdOrThrow(id: Long): Option =
    findByIdOrNull(id)
        ?: throw ElementNotFoundException("Element not found")

interface OptionJpaRepository : JpaRepository<Option, Long>
