package ecommerce.services.admin

import ecommerce.controller.admin.usecase.CreateOptionUseCase
import ecommerce.controller.admin.usecase.FindMembersWithRecentCartActivityUseCase
import ecommerce.controller.admin.usecase.FindTopProductsUseCase
import ecommerce.dto.ActiveMemberDTO
import ecommerce.dto.OptionDTO
import ecommerce.dto.TopProductDTO
import ecommerce.exception.NoSuchElementException
import ecommerce.mappers.toEntity
import ecommerce.repositories.CartItemRepository
import ecommerce.repositories.ProductRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminServiceImpl(
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
) : FindTopProductsUseCase, FindMembersWithRecentCartActivityUseCase, CreateOptionUseCase {
    @Transactional(readOnly = true)
    override fun findProducts(): List<TopProductDTO> {
        return cartItemRepository.findTop5ProductsAddedInLast30Days()
    }

    @Transactional(readOnly = true)
    override fun findMembers(): List<ActiveMemberDTO> {
        return cartItemRepository.findDistinctMembersWithCartActivityInLast7Days()
    }

    @Transactional
    override fun create(optionDTO: OptionDTO) {
        val product =
            productRepository.findByIdOrNull(optionDTO.productId!!)
                ?: throw NoSuchElementException("Product with id ${optionDTO.productId} doesn't exist")
        val option = optionDTO.toEntity(product)
        product.addOption(option)
        productRepository.save(product)
    }
}
