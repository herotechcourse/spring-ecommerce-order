//package ecommerce.service
//
//import org.mockito.Mockito.mock
//import ecommerce.dto.OptionResponse
//import ecommerce.dto.ProductRequest
//import ecommerce.entity.Option
//import ecommerce.entity.Product
//import ecommerce.exception.DuplicateProductNameException
//import ecommerce.repository.OptionJpaRepository
//import ecommerce.repository.ProductJpaRepository
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.assertThrows
//import org.mockito.ArgumentMatchers.any
//import org.springframework.data.domain.Page
//import org.springframework.data.domain.PageImpl
//import org.springframework.data.domain.PageRequest
//
//class ProductServiceTest {
//
//    private val productRepository: ProductJpaRepository = mock()
//    private val optionRepository: OptionJpaRepository = mock()
//    private val productService = ProductService(productRepository, optionRepository)
//
//    @Test
//    fun `create should save product when name is unique`() {
//        val request = ProductRequest(
//            name = "Protein Bar",
//            price = 12.0,
//            imageUrl = "http://img.com/bar.png",
//            options = listOf(Option("Vanilla", 10))
//        )
//
//        whenever(productRepository.existsByName("Protein Bar")).thenReturn(false)
//        whenever(productRepository.save(any())).thenAnswer { it.arguments[0] }
//
//        val saved = productService.create(request)
//
//        assertThat(saved.name).isEqualTo("Protein Bar")
//        assertThat(saved.options).hasSize(1)
//        verify(productRepository).save(any())
//    }
//
//    private fun whenever(existsByName: Boolean) {}
//
//    @Test
//    fun `create should throw exception when product name already exists`() {
//        val request = ProductRequest(
//            name = "Whey",
//            price = 20.0,
//            imageUrl = "http://image.png",
//            options = emptyList()
//        )
//
//        whenever(productRepository.existsByName("Whey")).thenReturn(true)
//
//        assertThrows<DuplicateProductNameException> {
//            productService.create(request)
//        }
//
//        verify(productRepository, never()).save(any())
//    }
//
//    @Test
//    fun `getAll should return all products`() {
//        val products = listOf(Product("A", 10.0), Product("B", 20.0))
//        whenever(productRepository.findAll()).thenReturn(products)
//
//        val result = productService.getAll()
//
//        assertThat(result).hasSize(2)
//        assertThat(result[0].name).isEqualTo("A")
//    }
//
//    @Test
//    fun `getById should return product when found`() {
//        val product = Product("C", 30.0)
//        whenever(productRepository.findById(1L)).thenReturn(Optional.of(product))
//
//        val result = productService.getById(1L)
//
//        assertThat(result).isEqualTo(product)
//    }
//
//    @Test
//    fun `getById should return null when not found`() {
//        whenever(productRepository.findById(999L)).thenReturn(Optional.empty())
//
//        val result = productService.getById(999L)
//
//        assertThat(result).isNull()
//    }
//
//    @Test
//    fun `getAllPaginated should return paginated results`() {
//        val pageable = PageRequest.of(0, 2)
//        val products = listOf(Product("X", 5.0), Product("Y", 6.0))
//        val page: Page<Product> = PageImpl(products, pageable, products.size.toLong())
//
//        whenever(productRepository.findAll(pageable)).thenReturn(page)
//
//        val result = productService.getAllPaginated(pageable)
//
//        assertThat(result.totalElements).isEqualTo(2)
//        assertThat(result.content[0].name).isEqualTo("X")
//    }
//
//    @Test
//    fun `getOptions should return mapped option responses`() {
//        val options = listOf(
//            Option(name = "Small", quantity = 5, id = 1L),
//            Option(name = "Large", quantity = 10, id = 2L)
//        )
//        whenever(optionRepository.findByProductId(1L)).thenReturn(options)
//
//        val result = productService.getOptions(1L)
//
//        assertThat(result).hasSize(2)
//        assertThat(result[0]).isEqualTo(OptionResponse(1L, "Small", 5))
//    }
//
//    @Test
//    fun `getOptions should throw when not found`() {
//        whenever(optionRepository.findByProductId(42L)).thenReturn(emptyList())
//
//        assertThrows<NoSuchElementException> {
//            productService.getOptions(42L)
//        }
//    }
//
//    @Test
//    fun `delete should call repository deleteById`() {
//        productService.delete(10L)
//        verify(productRepository).deleteById(10L)
//    }
//}