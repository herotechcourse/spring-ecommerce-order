package ecommerce.config.interceptor

import ecommerce.enums.UserRole
import ecommerce.infrastructure.JwtProvider
import ecommerce.model.User
import ecommerce.repository.UserRepository
import ecommerce.utils.exception.UnauthorisedUserException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component

@Component
class AdminInterceptor(
    userRepository: UserRepository,
    jwtProvider: JwtProvider,
) : BaseAuthInterceptor(jwtProvider, userRepository) {
    override fun handleAuthenticatedRequest(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        user: User,
    ): Boolean {
        if (user.role != UserRole.ADMIN) {
            throw UnauthorisedUserException("User role not admin")
        }

        return true
    }
}
