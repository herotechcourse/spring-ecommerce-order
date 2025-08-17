package ecommerce.service

import ecommerce.entity.Option
import ecommerce.repository.OptionJpaRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.Optional

@ExtendWith(MockKExtension::class)
class OptionServiceTest {

    @MockK
    private lateinit var optionRepo: OptionJpaRepository

    @InjectMockKs
    private lateinit var optionService: OptionService

    private val optionId = 1L
    private val quantity = 10


    @Test
    fun `decreaseQuantity should succeed when stock is sufficient`() {
        every { optionRepo.decrementStockIfEnough(optionId, quantity) } returns 1

        optionService.decreaseQuantity(optionId, quantity)

        verify(exactly = 1) { optionRepo.decrementStockIfEnough(optionId, quantity) }
    }

    @Test
    fun `decreaseQuantity should throw IllegalStateException when stock is insufficient`() {
        every { optionRepo.decrementStockIfEnough(optionId, quantity) } returns 0

        val mockOption = io.mockk.mockk<Option>()
        every { mockOption.quantity } returns 5
        every { optionRepo.findById(optionId) } returns Optional.of(mockOption)

        assertThrows<IllegalStateException> {
            optionService.decreaseQuantity(optionId, quantity)
        }
    }

    @Test
    fun `increaseQuantity should succeed when option exists`() {
        every { optionRepo.incrementStock(optionId, quantity) } returns 1

        optionService.increaseQuantity(optionId, quantity)

        verify(exactly = 1) { optionRepo.incrementStock(optionId, quantity) }
    }

    @Test
    fun `increaseQuantity should throw NoSuchElementException when option does not exist`() {
        every { optionRepo.incrementStock(optionId, quantity) } returns 0

        assertThrows<NoSuchElementException> {
            optionService.increaseQuantity(optionId, quantity)
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [0, -1])
    fun `service methods should throw IllegalArgumentException for non-positive quantity`(quantity: Int) {
        assertThrows<IllegalArgumentException> {
            optionService.decreaseQuantity(optionId, quantity)
        }

        assertThrows<IllegalArgumentException> {
            optionService.increaseQuantity(optionId, quantity)
        }
    }
}
