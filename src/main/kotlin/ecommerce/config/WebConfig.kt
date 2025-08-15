package ecommerce.config

import ecommerce.config.argumentResolver.LoginMemberArgumentResolver
import ecommerce.config.interceptor.AdminInterceptor
import ecommerce.config.interceptor.MemberInterceptor
import ecommerce.repository.UserRepository
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val adminInterceptor: AdminInterceptor,
    private val authInterceptor: MemberInterceptor,
    private val userRepository: UserRepository,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authInterceptor)
            .addPathPatterns("/api/member/cart/**", "/api/member/order/**")
        registry.addInterceptor(adminInterceptor)
            .addPathPatterns("/api/admin/products/**", "/api/admin/cart_statistics/**")
        super.addInterceptors(registry)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver?>) {
        val additionalResolvers =
            listOf(
                LoginMemberArgumentResolver(userRepository),
            )
        resolvers.addAll(additionalResolvers)
        super.addArgumentResolvers(resolvers)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders(HttpHeaders.LOCATION)
    }
}
