package ecommerce.service

import ecommerce.dto.CartItemRequest
import ecommerce.dto.CartItemResponse
import ecommerce.dto.PagedResponse
import ecommerce.exception.NotFoundException
import ecommerce.model.Cart
import ecommerce.model.CartItem
import ecommerce.repository.CartRepository
import ecommerce.repository.MemberRepository
import ecommerce.repository.ProductRepository
import ecommerce.service.mapper.CartItemMapper
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.min

@Transactional
@Service
class CartService(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
    private val memberRepository: MemberRepository,
) {
    fun findCart(memberId: Long): Cart {
        return cartRepository.findCartByMemberId(memberId)
            ?: throw RuntimeException("not found something ... ")
    }

    fun addItem(
        memberId: Long,
        request: CartItemRequest,
    ): CartItem {
        val member = memberRepository.findById(memberId).orElseThrow { NotFoundException() }
        val product =
            productRepository.findById(request.productId).orElseThrow { NotFoundException() }

        val cart =
            cartRepository.findCartByMemberId(memberId)
                ?: cartRepository.save(Cart(member))

        val item = cart.addItem(product, request.quantity)
        return item
    }

    fun deleteItem(
        memberId: Long,
        request: CartItemRequest,
    ) {
        val member = memberRepository.findById(memberId).orElseThrow { NotFoundException() }
        val product =
            productRepository.findById(request.productId).orElseThrow { NotFoundException() }

        val cart =
            cartRepository.findCartByMemberId(memberId)
                ?: cartRepository.save(Cart(member))

        cart.removeItem(product, request.quantity)
    }

    fun getPages(
        memberId: Long,
        page: Int,
        size: Int,
    ): PagedResponse<CartItemResponse> {
        val cart = findCart(memberId)
        val itemResponses = cart.items.map { CartItemMapper.toResponse(it) }
        val pageRequest = PageRequest.of(page, size, Sort.by("productName"))
        val start = pageRequest.offset.toInt()
        val end = min(start + pageRequest.pageSize, itemResponses.size)

        val pageContent = itemResponses.subList(start, end)
        val pageImpl = PageImpl<CartItemResponse>(pageContent, pageRequest, itemResponses.size.toLong())
        return CartItemMapper.toPagedResponse(pageImpl)
    }
}
