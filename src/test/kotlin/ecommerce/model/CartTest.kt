package ecommerce.model

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class CartTest {
    private lateinit var cart: Cart
    private lateinit var option1: Option
    private lateinit var option2: Option
    private lateinit var cartItem1: CartItem
    private lateinit var cartItem2: CartItem
    private lateinit var member: Member

    @BeforeEach
    fun setUp() {
        member = mock(Member::class.java)
        option1 = mock(Option::class.java)
        option2 = mock(Option::class.java)

        cart = Cart(member = member)
        cartItem1 = CartItem(option = option1, quantity = 2, cart = cart)
        cartItem2 = CartItem(option = option2, quantity = 1, cart = cart)
        cart.cartProducts.addAll(listOf(cartItem1, cartItem2))
    }

    @Test
    fun cleanCart() {
        assertTrue(cart.cartProducts.isNotEmpty())
        cart.cleanCart()
        assertTrue(cart.cartProducts.isEmpty())
        assertTrue(cart.cartProducts.all { it.cart == null })
    }
}
