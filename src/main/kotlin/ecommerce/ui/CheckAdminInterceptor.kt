package ecommerce.ui

import ecommerce.auth.AuthorizationExtractor
import ecommerce.auth.BearerAuthorizationExtractor
import ecommerce.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.HandlerInterceptor

class CheckAdminInterceptor(
    private val authService: AuthService,
) : HandlerInterceptor {
    private val authorizationExtractor: AuthorizationExtractor<String> = BearerAuthorizationExtractor()

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val token = authorizationExtractor.extract(request)
        val member = authService.findMemberByToken(token)
        if (member.role != "admin") {
            response.status = HttpStatus.FORBIDDEN.value()
            return false
        }

        return true
    }
}
