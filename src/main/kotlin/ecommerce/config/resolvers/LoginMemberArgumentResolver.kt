package ecommerce.config.resolvers

import ecommerce.annotation.LoginMember
import ecommerce.exception.AuthorizationException
import ecommerce.mappers.toDTO
import ecommerce.mappers.toLoginDTO
import ecommerce.repositories.MemberRepository
import org.springframework.core.MethodParameter
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class LoginMemberArgumentResolver(
    private val memberRepository: MemberRepository,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(LoginMember::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any {
        val email =
            webRequest.getAttribute("email", RequestAttributes.SCOPE_REQUEST) as? String
                ?: throw AuthorizationException("Email attribute missing")
        return memberRepository.findByEmail(email)?.toDTO()?.toLoginDTO() ?: throw EmptyResultDataAccessException(
            "Member with Email $email not found",
            1,
        )
    }
}
