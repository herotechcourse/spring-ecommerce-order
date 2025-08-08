package ecommerce.service

import ecommerce.dto.OptionRequest
import ecommerce.dto.ProductRequest
import ecommerce.entity.Option
import ecommerce.entity.Product
import ecommerce.exception.DuplicateProductNameException
import ecommerce.repository.OptionJpaRepository
import ecommerce.repository.ProductJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional

class ProductServiceTest {
    private lateinit var productRepository: ProductJpaRepository
    private lateinit var optionRepository: OptionJpaRepository
    private lateinit var service: ProductService

    @BeforeEach
    fun setUp() {
        productRepository = mock(ProductJpaRepository::class.java)
        optionRepository = mock(OptionJpaRepository::class.java)
        service = ProductService(productRepository, optionRepository)
    }

    @Test
    fun `getAll should return all products mapped to ProductResponse`() {
        val product = Product("Bar", 1.0, "url", emptyList(), 1L)
        `when`(productRepository.findAll()).thenReturn(listOf(product))

        val result = service.getAll()

        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("Bar")
    }

    @Test
    fun `getById should return mapped ProductResponse when product exists`() {
        val product = Product("Bar", 1.0, "url", emptyList(), 1L)
        `when`(productRepository.findById(1L)).thenReturn(
            Optional.of(product),
        )

        val result = service.getById(1L)

        assertThat(result?.name).isEqualTo("Bar")
    }

    @Test
    fun `getById should return null if product not found`() {
        `when`(productRepository.findById(999L)).thenReturn(Optional.empty())

        val result = service.getById(999L)

        assertThat(result).isNull()
    }

    @Test
    fun `getAllPaginated should return paginated ProductResponse`() {
        val pageable = PageRequest.of(0, 2)
        val product = Product("Bar", 1.0, "url", emptyList(), 1L)
        val page: Page<Product> = PageImpl(listOf(product), pageable, 1)

        `when`(productRepository.findAll(pageable)).thenReturn(page)

        val result = service.getAllPaginated(pageable)

        assertThat(result.totalElements).isEqualTo(1)
        assertThat(result.content[0].name).isEqualTo("Bar")
    }

    @Test
    fun `getOptions should throw when product has no options`() {
        `when`(optionRepository.findByProductId(42L)).thenReturn(emptyList())

        assertThrows<NoSuchElementException> {
            service.getOptions(42L)
        }
    }

    @Test
    fun `create should save product with options`() {
        val request =
            ProductRequest(
                name = "New",
                price = 2.0,
                imageUrl = "url",
                options = listOf(OptionRequest("S", 1)),
            )

        val savedProduct =
            Product(
                name = "New",
                price = 2.0,
                imageUrl = "url",
                options = listOf(Option("S", 1)),
                id = 1L,
            )

        `when`(productRepository.existsByName("New")).thenReturn(false)
        `when`(productRepository.save(any(Product::class.java))).thenReturn(savedProduct)

        val result = service.create(request)

        assertThat(result.name).isEqualTo("New")
        assertThat(result.options).hasSize(1)
        assertThat(result.options[0].name).isEqualTo("S")
    }

    @Test
    fun `create should throw when name exists`() {
        `when`(productRepository.existsByName("Dup")).thenReturn(true)

        val request = ProductRequest("Dup", 1.0, "url", listOf())

        assertThrows<DuplicateProductNameException> {
            service.create(request)
        }

        verify(productRepository, never()).save(any())
    }

    @Test
    fun `update should replace product and return response`() {
        val id = 10L
        val oldProduct = Product("Old", 1.0, "url", emptyList(), id)
        val request = ProductRequest("Updated", 2.0, "img", listOf(OptionRequest("Opt", 1)))
        val updated = Product("Updated", 2.0, "img", emptyList(), id)

        `when`(productRepository.findById(id)).thenReturn(Optional.of(oldProduct))
        `when`(productRepository.existsByNameAndIdNot("Updated", id)).thenReturn(false)
        `when`(productRepository.save(any(Product::class.java))).thenReturn(updated)

        val result = service.update(id, request)

        assertThat(result.name).isEqualTo("Updated")
    }

    @Test
    fun `update should throw if product not found`() {
        `when`(productRepository.findById(999L)).thenReturn(Optional.empty())

        val request = ProductRequest("Updated", 1.0, "url", listOf())

        assertThrows<NoSuchElementException> {
            service.update(999L, request)
        }
    }

    @Test
    fun `delete should call repository deleteById`() {
        service.delete(5L)
        verify(productRepository).deleteById(5L)
    }
}
