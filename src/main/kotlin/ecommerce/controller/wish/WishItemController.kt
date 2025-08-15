package ecommerce.controller.wish

import ecommerce.annotation.LoginMember
import ecommerce.controller.wish.usecase.CrudWishItemUseCase
import ecommerce.dto.MemberLoginDTO
import ecommerce.dto.WishItemRequestDTO
import ecommerce.dto.WishItemResponseDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/wish")
class WishItemController(private val crudWishItemUseCase: CrudWishItemUseCase) {
    @GetMapping
    fun getAllByMember(
        @LoginMember member: MemberLoginDTO,
        @PageableDefault(size = 10, direction = Sort.Direction.ASC)
        page: Pageable,
    ): Page<WishItemResponseDTO> {
        return crudWishItemUseCase.findByMember(member.id, page = page)
    }

    @PostMapping
    fun saveWishItem(
        @RequestBody wishItemRequestDTO: WishItemRequestDTO,
        @LoginMember member: MemberLoginDTO,
    ): ResponseEntity<WishItemResponseDTO> {
        val wishItemResponseDTO = crudWishItemUseCase.save(wishItemRequestDTO, member.id)
        return ResponseEntity.ok().body(wishItemResponseDTO)
    }

    @DeleteMapping
    fun deleteWishItem(
        @RequestBody wishItemRequestDTO: WishItemRequestDTO,
        @LoginMember member: MemberLoginDTO,
    ): ResponseEntity<Unit> {
        crudWishItemUseCase.delete(wishItemRequestDTO, member.id)
        return ResponseEntity.noContent().build()
    }
}
