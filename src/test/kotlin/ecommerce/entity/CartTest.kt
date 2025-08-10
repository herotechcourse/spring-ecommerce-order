package ecommerce.entity

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CartTest {
    @Test
    fun `add should add new item and update timestamp`() {
        val cart = Cart(mockk(relaxed = true))
        val before = cart.updatedAt
        val o1 = option(1L)

        cart.add(o1, 2)

        assertThat(cart.items.size).isEqualTo(1)
        assertThat(cart.items.first().quantity).isEqualTo(2)
        assertThat(cart.updatedAt.isBefore(before)).isFalse()
    }

    @Test
    fun `add same option should increase quantity, not add new item`() {
        val cart = Cart(member = mockk(relaxed = true))
        val o1 = option(1L)

        cart.add(o1, 2)
        cart.add(o1, 3)

        assertThat(cart.items.size).isEqualTo(1)
        assertThat(cart.items.first().quantity).isEqualTo(5)
    }

    @Test
    fun `add with non-positive quantity should fail`() {
        val cart = Cart(member = mockk(relaxed = true))
        val o1 = option(1L)
        assertThrows<IllegalArgumentException> {
            cart.add(o1, 0)
        }
        assertThrows<IllegalArgumentException> {
            cart.add(o1, -1)
        }
    }

    @Test
    fun `remove part of quantity should decrease and update timestamp`() {
        val cart = Cart(member = mockk(relaxed = true))
        val o1 = option(1L)
        cart.add(o1, 5)
        val before = cart.updatedAt

        cart.remove(optionId = 1L, quantity = 3)

        assertThat(cart.items.size).isEqualTo(1)
        assertThat(cart.items.first().quantity).isEqualTo(2)
        assertThat(cart.updatedAt.isBefore(before)).isFalse()
    }

    @Test
    fun `remove exact quantity should remove item`() {
        val cart = Cart(member = mockk(relaxed = true))
        val o1 = option(1L)
        cart.add(o1, 5)

        cart.remove(optionId = 1L, quantity = 5)

        assertThat(cart.items.isEmpty()).isTrue()
    }

    @Test
    fun `remove more than in cart should fail`() {
        val cart = Cart(member = mockk(relaxed = true))
        val o1 = option(1L)
        cart.add(o1, 5)

        assertThrows<IllegalArgumentException> {
            cart.remove(optionId = 1L, quantity = 6)
        }
    }

    @Test
    fun `remove non-existing option should fail`() {
        val cart = Cart(member = mockk(relaxed = true))
        assertThrows<NoSuchElementException> {
            cart.remove(optionId = 42L, quantity = 1)
        }
    }

    @Test
    fun `remove with non-positive quantity should fail`() {
        val cart = Cart(member = mockk(relaxed = true))
        val o1 = option(1L)
        cart.add(o1, 2)

        assertThrows<IllegalArgumentException> {
            cart.remove(1L, 0)
        }
        assertThrows<IllegalArgumentException> {
            cart.remove(1L, -2)
        }
    }

    private fun option(
        id: Long,
        name: String = "option",
        quantity: Int = 100,
    ): Option {
        val product = mockk<Product>(relaxed = true)
        val opt = mockk<Option>()
        every { opt.id } returns id
        every { opt.name } returns OptionName(name)
        every { opt.quantity } returns quantity
        every { opt.product } returns product
        return opt
    }
}
