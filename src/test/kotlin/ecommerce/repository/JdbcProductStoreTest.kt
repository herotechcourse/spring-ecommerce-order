package ecommerce.repository

import ecommerce.model.Product
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.jdbc.core.JdbcTemplate

@JdbcTest
class JdbcProductStoreTest {
    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    private lateinit var productStore: JdbcProductStore

    @BeforeEach
    fun setUp() {
        productStore = JdbcProductStore(jdbcTemplate)
    }

    @Test
    fun `should return all products`() {
        val products = productStore.findAll()
        Assertions.assertThat(products).isNotEmpty
        Assertions.assertThat(products.map { it.name }).contains("Phone", "Laptop")
    }

    @Test
    fun `should find product by id`() {
        val product = productStore.findById(2L)
        Assertions.assertThat(product).isNotNull
        Assertions.assertThat(product.name).isEqualTo("Phone")
    }

    @Test
    fun `should find product by name`() {
        val product = productStore.findByName(name = "Phone")
        Assertions.assertThat(product).isNotNull
        Assertions.assertThat(product?.name).isEqualTo("Phone")
    }

    @Test
    fun `existsByName returns true when product is present`() {
        jdbcTemplate.update(
            "INSERT INTO PRODUCTS(product_name, price, image_url) VALUES (?,?,?)",
            "Tablet",
            299.99,
            "http://image/tablet.jpg",
        )

        assertThat(productStore.existsByName("Tablet")).isTrue
    }

    @Test
    fun `existsByName returns false when product is absent`() {
        assertThat(productStore.existsByName("NonExistent")).isFalse
    }

    @Test
    fun `should save a new product`() {
        val newProduct = Product(name = "Tablet", price = 299.99, imageUrl = "http://tablet.jpg")
        productStore.save(newProduct) // no return value

        val savedProducts = productStore.findAll()
        Assertions.assertThat(savedProducts).anyMatch { it.name == "Tablet" && it.price == 299.99 }
    }

    @Test
    fun `should update existing product`() {
        val updatedProduct = Product(name = "Updated Phone", price = 499.99, imageUrl = "http://updated_phone.jpg")
        productStore.update(1L, updatedProduct)

        val product = productStore.findById(1L)
        Assertions.assertThat(product.name).isEqualTo("Updated Phone")
        Assertions.assertThat(product.price).isEqualTo(499.99)
        Assertions.assertThat(product.imageUrl).isEqualTo("http://updated_phone.jpg")
    }

    @Test
    fun `should delete a product`() {
        val isDeleted = productStore.delete(1L)
        Assertions.assertThat(isDeleted).isEqualTo(true)
    }
}
