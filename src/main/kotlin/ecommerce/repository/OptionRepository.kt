package ecommerce.repository

import ecommerce.model.Option
import org.springframework.data.jpa.repository.JpaRepository

interface OptionRepository : JpaRepository<Option, Long>
