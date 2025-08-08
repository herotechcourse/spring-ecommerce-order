package ecommerce

import ecommerce.service.AuthService
import ecommerce.ui.CheckAdminInterceptor
import ecommerce.ui.LoginMemberArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfiguration(
    private val loginMemberArgumentResolver: LoginMemberArgumentResolver,
    private val authService: AuthService,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(CheckAdminInterceptor(authService))
            .addPathPatterns("/api/admin/**")
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(loginMemberArgumentResolver)
    }
}
