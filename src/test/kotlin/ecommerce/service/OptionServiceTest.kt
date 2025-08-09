package ecommerce.service

import ecommerce.dto.OptionCreateDto
import ecommerce.entity.OptionEntity
import ecommerce.entity.ProductEntity
import ecommerce.repository.OptionRepositoryJpa
import ecommerce.repository.ProductRepositoryJpa
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class OptionServiceTest
    @Autowired
    constructor(
        private val optionService: OptionService,
        private val productRepositoryJpa: ProductRepositoryJpa,
        private val optionRepositoryJpa: OptionRepositoryJpa,
    ) {
        @Test
        fun `createProductWithOptions should throw when no options are passed`() {
            val ex =
                assertThrows<IllegalArgumentException> {
                    optionService.createProductWithOptions(
                        name = "Product1",
                        price = 19.99,
                        imageUrl = "http://example.com/tshirt.jpg",
                        optionsDto = emptyList(),
                    )
                }
            assertTrue(ex.message!!.contains("must have at least one option"))
        }

        @Test
        fun `createProductWithOptions should persist product and its options`() {
            val optionsDto =
                listOf(
                    OptionCreateDto(name = "option1", quantity = 5),
                    OptionCreateDto(name = "option2", quantity = 10),
                )

            // creating a product with a temporary option to avoid the required no-empty rule for option param
            val product =
                ProductEntity(
                    name = "Product1",
                    price = 19.99,
                    imageUrl = "http://example.com/tshirt.jpg",
                    options = optionsDto.map { OptionEntity(name = it.name, quantity = it.quantity) }.toMutableList(),
                )

            product.options.forEach { it.product = product }
            productRepositoryJpa.saveAndFlush(product)

            val savedProduct = productRepositoryJpa.findById(product.id!!).orElseThrow()
            assertEquals("Product1", savedProduct.name)
            assertEquals(2, savedProduct.options.size)
            assertTrue(savedProduct.options.any { it.name == "option1" && it.quantity == 5L })
            assertTrue(savedProduct.options.any { it.name == "option2" && it.quantity == 10L })
        }

        @Test
        fun `decreaseOptionQuantity should reduce quantity of existing option`() {
            val option = OptionEntity(name = "option1", quantity = 8)
            val product =
                ProductEntity(
                    name = "product1",
                    price = 3.99,
                    imageUrl = "http://example.com/product1.jpg",
                    options = mutableListOf(option),
                )
            option.product = product
            productRepositoryJpa.saveAndFlush(product)

            val savedOption = product.options.first()
            optionService.decreaseOptionQuantity(savedOption.id!!, 3)

            val updatedOption = optionRepositoryJpa.findById(savedOption.id!!).orElseThrow()
            assertEquals(5L, updatedOption.quantity)
        }

        @Test
        fun `decreaseOptionQuantity should throw when option does not exist`() {
            val exception =
                assertThrows(NoSuchElementException::class.java) {
                    optionService.decreaseOptionQuantity(9999L, 1)
                }
            assertEquals("Option not found id=9999", exception.message)
        }
    }
