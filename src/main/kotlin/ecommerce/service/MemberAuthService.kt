package ecommerce.service

import ecommerce.dto.auth.AuthTokenPayload
import ecommerce.dto.auth.LoginRequest
import ecommerce.dto.user.UserCreateResponse
import ecommerce.dto.user.UserRequestDTO
import ecommerce.enums.UserRole
import ecommerce.infrastructure.JwtProvider
import ecommerce.model.Cart
import ecommerce.model.User
import ecommerce.repository.CartRepository
import ecommerce.repository.UserRepository
import ecommerce.utils.exception.UserAlreadyExistsException
import org.springframework.stereotype.Service
import java.net.URI

@Service
class MemberAuthService(
    private val userRepository: UserRepository,
    private val cartRepository: CartRepository,
    private val jwtProvider: JwtProvider,
    private val loginService: LoginService,
) {
    fun signUp(userRequestDTO: UserRequestDTO): UserCreateResponse {
        if (userRepository.existsByEmail(userRequestDTO.email)) {
            throw UserAlreadyExistsException(userRequestDTO.email)
        }
        val member =
            User(
                userRequestDTO.email,
                userRequestDTO.password,
                userRequestDTO.name,
                UserRole.USER,
            )

        val savedMember = userRepository.save(member)

        cartRepository.save(Cart(member))

        val authTokenPayload = jwtProvider.createToken(AuthTokenPayload(member.email))
        return UserCreateResponse(URI.create("/users/$savedMember.id"), "Bearer $authTokenPayload")
    }

    fun login(loginRequest: LoginRequest): String {
        return loginService.login(loginRequest)
    }
}
