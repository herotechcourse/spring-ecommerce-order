package ecommerce.services.cart

import ecommerce.controller.cart.usecase.ManageCartItemUseCase
import ecommerce.controller.member.usecase.CrudMemberUseCase
import ecommerce.dto.CartItemRequestDTO
import ecommerce.dto.CartItemResponseDTO
import ecommerce.entities.CartItemEntity
import ecommerce.exception.OperationFailedException
import ecommerce.mappers.toDTO
import ecommerce.mappers.toEntity
import ecommerce.repositories.CartItemRepository
import ecommerce.repositories.OptionRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CartItemServiceImpl(
    private val cartItemRepository: CartItemRepository,
    private val optionRepository: OptionRepository,
    private val memberService: CrudMemberUseCase,
) : ManageCartItemUseCase {
    @Transactional
    override fun addOrUpdate(
        cartItemRequestDTO: CartItemRequestDTO,
        memberId: Long,
    ): CartItemResponseDTO {
        validateOptionExists(cartItemRequestDTO.optionId)

        val cartItem =
            if (!cartItemRepository.existsByOptionIdAndMemberId(cartItemRequestDTO.optionId, memberId)) {
                handleCreate(cartItemRequestDTO, memberId)
            } else {
                handleUpdate(cartItemRequestDTO, memberId)
            }

        return cartItem.toDTO()
    }

    @Transactional(readOnly = true)
    override fun findByMember(memberId: Long): List<CartItemResponseDTO> {
        val itemsWithProducts = cartItemRepository.findByMemberId(memberId)

        return itemsWithProducts.map { cartItem ->
            CartItemResponseDTO(
                id = cartItem.id,
                memberId = cartItem.member.id,
                option = cartItem.option.toDTO(),
                quantity = cartItem.quantity,
                addedAt = cartItem.addedAt,
            )
        }
    }

    @Transactional
    override fun delete(
        cartItemRequestDTO: CartItemRequestDTO,
        memberId: Long,
    ) {
        cartItemRepository.deleteByOptionIdAndMemberId(cartItemRequestDTO.optionId, memberId)
    }

    private fun validateOptionExists(optionId: Long) {
        if (!optionRepository.existsById(optionId)) {
            throw EmptyResultDataAccessException("Product with ID $optionId does not exist", 1)
        }
    }

    @Transactional
    override fun deleteAll() {
        cartItemRepository.deleteAll()
    }

    private fun handleCreate(
        cartItemRequestDTO: CartItemRequestDTO,
        memberId: Long,
    ): CartItemEntity {
        val option =
            optionRepository.findByIdOrNull(cartItemRequestDTO.optionId)
                ?: throw OperationFailedException("Invalid Product Id ${cartItemRequestDTO.optionId}")
        option.checkStock(cartItemRequestDTO.quantity)
        val member = memberService.findById(memberId)
        return cartItemRepository.save(
            CartItemEntity(
                member = member.toEntity(),
                option = option,
                quantity = cartItemRequestDTO.quantity,
                addedAt = LocalDateTime.now(),
            ),
        )
    }

    private fun handleUpdate(
        cartItemRequestDTO: CartItemRequestDTO,
        memberId: Long,
    ): CartItemEntity {
        val existing =
            cartItemRepository
                .findByOptionIdAndMemberId(cartItemRequestDTO.optionId, memberId)
                ?: throw OperationFailedException("Cart item not found")

        if (cartItemRequestDTO.quantity <= 0) throw OperationFailedException("Quantity must be greater than zero")
        if (existing.quantity == cartItemRequestDTO.quantity) return existing

        existing.quantity = cartItemRequestDTO.quantity
        return cartItemRepository.save(existing)
    }
}
