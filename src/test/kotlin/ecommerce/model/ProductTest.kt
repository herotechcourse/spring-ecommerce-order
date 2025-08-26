package ecommerce.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ProductTest {
    @Test
    fun `should create a new product`() {
        val options = listOf(Option(name = "size", quantity = 10), Option(name = "color", quantity = 5))
        val product = Product("lotion", 20.0, "https://lotion.jpeg", options)
        assertThat(product).isNotNull()
        assertThat(product.name).isEqualTo("lotion")
        assertThat(product.price).isEqualTo(20.0)
        assertThat(product.imageUrl).isEqualTo("https://lotion.jpeg")
        assertThat(product.options).hasSize(2)
    }

    @Test
    fun `should change name of product`() {
        val product =
            Product("lotion", 20.0, "https://lotion.jpeg", options = listOf(Option(name = "size", quantity = 10)))
        product.changeName("spf")
        assertThat(product.name).isEqualTo("spf")
    }

    @Test
    fun `should change price of product`() {
        val product =
            Product("lotion", 20.0, "https://lotion.jpeg", options = listOf(Option(name = "size", quantity = 10)))
        product.changePrice(25.0)
        assertThat(product.price).isEqualTo(25.0)
    }

    @Test
    fun `should change image of product`() {
        val product =
            Product("lotion", 20.0, "https://lotion.jpeg", options = listOf(Option(name = "size", quantity = 10)))
        product.changeImageUrl("https://lotion2.jpeg")
        assertThat(product.imageUrl).isEqualTo("https://lotion2.jpeg")
    }
}
