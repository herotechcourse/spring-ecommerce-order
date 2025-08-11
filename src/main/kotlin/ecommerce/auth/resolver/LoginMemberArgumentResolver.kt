package ecommerce.auth.resolver

import ecommerce.auth.annotation.LoginMember
import ecommerce.dto.AuthenticatedMember
import ecommerce.exception.UnauthorizedException
import ecommerce.service.MemberService
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class LoginMemberArgumentResolver(
    private val memberService: MemberService,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(LoginMember::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): AuthenticatedMember {
        val token =
            webRequest.getHeader("Authorization")
                ?.removePrefix("Bearer ")
                ?: throw UnauthorizedException()

        val member = memberService.findByToken(token) ?: throw UnauthorizedException()
        return AuthenticatedMember.from(member)
    }
}
