package ecommerce.repository

import ecommerce.entity.Option
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface OptionJpaRepository : JpaRepository<Option, Long> {
    fun findByProductId(productId: Long): List<Option>

    fun findByProductIdAndId(
        productId: Long,
        id: Long,
    ): Option?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        update Option o
        set o.quantity = o.quantity - :qty
        where o.id = :optionId
          and o.quantity >= :qty
    """
    )
    fun decrementStockIfEnough(
        optionId: Long,
        qty: Int,
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        update Option o
        set o.quantity = o.quantity + :qty
        where o.id = :optionId
    """
    )
    fun incrementStock(
        optionId: Long,
        qty: Int,
    ): Int
}
