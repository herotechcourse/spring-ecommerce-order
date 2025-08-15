package ecommerce.service

import ecommerce.dto.auth.AuthTokenPayload
import ecommerce.dto.auth.LoginRequest
import ecommerce.enums.UserRole
import ecommerce.infrastructure.JwtProvider
import ecommerce.repository.UserRepository
import ecommerce.utils.exception.UserCredentialException
import org.springframework.stereotype.Service

@Service
class LoginService(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
) {
    fun login(
        loginRequest: LoginRequest,
        expectedRole: UserRole = UserRole.USER,
    ): String {
        val user =
            userRepository.findByEmailAndPassword(
                loginRequest.email,
                loginRequest.password,
            ).orElseThrow { UserCredentialException() }

        if (user.role != expectedRole) {
            throw UserCredentialException("Incorrect role for this endpoint")
        }

        val token =
            jwtProvider.createToken(
                AuthTokenPayload(user.email),
            )
        return "Bearer $token"
    }
}
