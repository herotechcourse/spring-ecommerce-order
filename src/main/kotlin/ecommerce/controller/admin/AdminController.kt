package ecommerce.controller.admin

import ecommerce.annotation.CheckAdminOnly
import ecommerce.annotation.IgnoreCheckLogin
import ecommerce.controller.admin.usecase.CreateOptionUseCase
import ecommerce.controller.admin.usecase.FindMembersWithRecentCartActivityUseCase
import ecommerce.controller.admin.usecase.FindTopProductsUseCase
import ecommerce.dto.ActiveMemberDTO
import ecommerce.dto.OptionDTO
import ecommerce.dto.TopProductDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
@CheckAdminOnly
class AdminController(
    private val createOptionUseCase: CreateOptionUseCase,
    private val findTopProductsUseCase: FindTopProductsUseCase,
    private val findMembersWithRecentCartActivityUseCase: FindMembersWithRecentCartActivityUseCase,
) {
    @GetMapping("/top-products")
    fun getTopProducts(): List<TopProductDTO> = findTopProductsUseCase.findProducts()

    @GetMapping("/active-members")
    fun getActiveMembers(): List<ActiveMemberDTO> = findMembersWithRecentCartActivityUseCase.findMembers()

    @PostMapping("/option")
    fun createOption(
        @RequestBody optionDTO: OptionDTO,
    ): ResponseEntity<Unit> {
        createOptionUseCase.create(optionDTO)
        return ResponseEntity.ok().build()
    }

    @IgnoreCheckLogin
    @GetMapping("/slow")
    fun slow(): String {
        Thread.sleep(15_000) // simulate 15s delay
        return "Done"
    }
}
