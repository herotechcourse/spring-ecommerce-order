package ecommerce.controller

import ecommerce.dto.MemberRequest
import ecommerce.dto.TokenMemberResponse
import ecommerce.service.MemberService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService,
) {
    @PostMapping("/register")
    fun register(
        @RequestBody @Valid request: MemberRequest,
    ): ResponseEntity<TokenMemberResponse> {
        val token = memberService.register(request)
        return ResponseEntity.status(201).body(token)
    }

    @PostMapping("/login")
    fun login(
        @RequestBody @Valid request: MemberRequest,
    ): ResponseEntity<TokenMemberResponse> {
        val token = memberService.login(request)
        return ResponseEntity.ok(token)
    }
}
