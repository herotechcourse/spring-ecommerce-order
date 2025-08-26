package ecommerce.controller.api

import ecommerce.auth.AuthorizationExtractor
import ecommerce.auth.BearerAuthorizationExtractor
import ecommerce.dto.auth.AuthResponse
import ecommerce.dto.member.LoginForm
import ecommerce.dto.member.MemberResponse
import ecommerce.dto.member.RegisterForm
import ecommerce.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/members")
class AuthController(
    private val authService: AuthService,
    private val authorizationExtractor: AuthorizationExtractor<String> = BearerAuthorizationExtractor(),
) {
    @PostMapping("/register")
    fun registerMember(
        @RequestBody @Valid form: RegisterForm,
    ): ResponseEntity<AuthResponse> {
        val member = authService.registerMember(form)
        val uri = URI.create("/api/members/${member.id}")
        val authResponse = authService.loginMember(LoginForm.fromRegisterForm(form))
        return ResponseEntity.created(uri).body(authResponse)
    }

    @PostMapping("/login")
    fun loginMember(
        @RequestBody @Valid form: LoginForm,
    ): ResponseEntity<AuthResponse> {
        val authResponse = authService.loginMember(form)
        return ResponseEntity.ok(authResponse)
    }

    @GetMapping("/me")
    fun findMyInformation(request: HttpServletRequest): ResponseEntity<MemberResponse> {
        val token = authorizationExtractor.extract(request)
        val member = authService.findMemberByToken(token)
        val response = MemberResponse(member.id, member.email)
        return ResponseEntity.ok().body(response)
    }
}
