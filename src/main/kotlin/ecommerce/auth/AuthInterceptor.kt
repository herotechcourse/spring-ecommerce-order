package ecommerce.auth

import ecommerce.service.MemberService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthInterceptor(
    private val memberService: MemberService,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val token = request.getHeader("Authorization")?.removePrefix("Bearer ") ?: return unauthorized(response)

        val member =
            try {
                memberService.findByToken(token)
            } catch (e: Exception) {
                return unauthorized(response)
            }

        if (request.requestURI.startsWith("/admin") && member.role != "ADMIN") {
            return unauthorized(response)
        }

        return true
    }

    private fun unauthorized(response: HttpServletResponse): Boolean {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        return false
    }
}
