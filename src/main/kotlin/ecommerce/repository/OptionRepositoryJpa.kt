package ecommerce.repository

import ecommerce.entity.OptionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OptionRepositoryJpa : JpaRepository<OptionEntity, Long>
