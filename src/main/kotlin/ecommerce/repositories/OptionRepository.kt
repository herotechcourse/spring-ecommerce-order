package ecommerce.repositories

import ecommerce.entities.OptionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OptionRepository : JpaRepository<OptionEntity, Long>
