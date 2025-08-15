package ecommerce.service

import ecommerce.dto.cartProduct.CartProductDto
import ecommerce.dto.cartProduct.CartProductResponse
import ecommerce.enums.CartAction
import ecommerce.infrastructure.ApplicationLogger
import ecommerce.model.Cart
import ecommerce.model.CartProduct
import ecommerce.model.Option
import ecommerce.model.User
import ecommerce.repository.CartProductRepository
import ecommerce.repository.CartRepository
import ecommerce.repository.OptionRepository
import ecommerce.utils.exception.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CartService(
    private val optionRepository: OptionRepository,
    private val cartRepository: CartRepository,
    private val cartProductRepository: CartProductRepository,
    private val adminStatisticsService: AdminStatisticsService,
) {
    val logger = ApplicationLogger()

    fun getCartProducts(member: User): CartProductResponse {
        val cart = getCart(member)
        return CartProductResponse(
            cart.items.map { it.toDTO() },
        )
    }

    fun addProductToCart(
        member: User,
        optionId: Long,
    ): Long {
        val cart = getCart(member)
        val option = getValidProductOption(optionId)
        val addedItem = cart.addProduct(option)
        cartProductRepository.save(addedItem)

        recordStatistic(member, option, CartAction.ADD)

        return addedItem.id
    }

    fun removeProductFromCart(
        member: User,
        optionId: Long,
    ) {
        val cart = getCart(member)
        val option = getValidProductOption(optionId)

        cart.decrementProduct(option)

        recordStatistic(member, option, CartAction.DELETE)
    }

    fun clearCart(member: User) {
        val cart = getCart(member)

        if (cart.items.isEmpty()) {
            throw EntityNotFoundException("No items found")
        }

        cart.items.forEach {
            recordStatistic(member, it.option, CartAction.DELETE)
        }
        cart.clear()
    }

    fun checkoutCart(member: User) {
        val cart = getCart(member)
        cart.items.forEach {
            optionRepository.findById(it.option.id).orElseThrow {
                EntityNotFoundException("No items found")
            }.decrementQuantity(it.quantity)
        }
        cart.clear()
        cartRepository.save(cart)
    }

    private fun recordStatistic(
        member: User,
        option: Option,
        action: CartAction,
    ) {
        try {
            adminStatisticsService.createStatistic(member, option, action)
        } catch (e: Exception) {
            logger.logError("${e.message}")
        }
    }

    private fun getCart(member: User): Cart {
        return cartRepository.findByUserIdOrUserNull(member.id)
            ?: throw EntityNotFoundException("Cart not found")
    }

    private fun getValidProductOption(optionId: Long): Option {
        return optionRepository.findById(optionId)
            .orElseThrow { EntityNotFoundException("Product option not found") }
    }

    private fun CartProduct.toDTO(): CartProductDto {
        return CartProductDto(
            option.id,
            option.name,
            option.price,
            option.imageUrl,
            quantity,
        )
    }
}
