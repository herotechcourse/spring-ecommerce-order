package ecommerce.services.wish

import ecommerce.controller.member.usecase.CrudMemberUseCase
import ecommerce.controller.wish.usecase.CrudWishItemUseCase
import ecommerce.dto.WishItemRequestDTO
import ecommerce.dto.WishItemResponseDTO
import ecommerce.entities.WishItemEntity
import ecommerce.exception.OperationFailedException
import ecommerce.mappers.toDTO
import ecommerce.mappers.toEntity
import ecommerce.repositories.ProductRepository
import ecommerce.repositories.WishItemRepository
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class WishItemServiceImpl(
    private val wishItemRepository: WishItemRepository,
    private val productRepository: ProductRepository,
    private val memberService: CrudMemberUseCase,
) : CrudWishItemUseCase {
    @Transactional
    override fun save(
        wishItemRequestDTO: WishItemRequestDTO,
        memberId: Long,
    ): WishItemResponseDTO {
        validateProductExists(wishItemRequestDTO.productId)

        val product =
            productRepository.findByIdOrNull(wishItemRequestDTO.productId)
                ?: throw OperationFailedException("Invalid Product Id ${wishItemRequestDTO.productId}")
        val member = memberService.findById(memberId)
        return wishItemRepository.save(
            WishItemEntity(
                member = member.toEntity(),
                product = product,
                addedAt = LocalDateTime.now(),
            ),
        ).toDTO()
    }

    @Transactional(readOnly = true)
    override fun findByMember(memberId: Long): List<WishItemResponseDTO> {
        val wishItems = wishItemRepository.findByMemberId(memberId)

        return wishItems.map { cartItem ->
            WishItemResponseDTO(
                id = cartItem.id,
                memberId = cartItem.member.id,
                product = cartItem.product.toDTO(),
                addedAt = cartItem.addedAt,
            )
        }
    }

    @Transactional(readOnly = true)
    override fun findByMember(
        memberId: Long,
        page: Pageable,
    ): Page<WishItemResponseDTO> {
        val wishItems = wishItemRepository.findByMemberId(memberId, page)

        return wishItems.map { cartItem ->
            WishItemResponseDTO(
                id = cartItem.id,
                memberId = cartItem.member.id,
                product = cartItem.product.toDTO(),
                addedAt = cartItem.addedAt,
            )
        }
    }

    @Transactional
    override fun delete(
        wishItemRequestDTO: WishItemRequestDTO,
        memberId: Long,
    ) {
        wishItemRepository.deleteByProductIdAndMemberId(wishItemRequestDTO.productId, memberId)
    }

    @Transactional
    override fun deleteAll() {
        wishItemRepository.deleteAll()
    }

    private fun validateProductExists(productId: Long) {
        if (!productRepository.existsById(productId)) {
            throw EmptyResultDataAccessException("Product with ID $productId does not exist", 1)
        }
    }
}
