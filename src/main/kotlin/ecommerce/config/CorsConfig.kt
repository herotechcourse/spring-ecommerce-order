package ecommerce.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig(
    @Value("\${app.cors.allowed-origins:*}") private val allowedOriginsCsv: String,
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        val origins =
            allowedOriginsCsv.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

        val mapping =
            registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders(HttpHeaders.LOCATION)
                .maxAge(1800)

        if (origins.size == 1 && origins[0] == "*") {
            mapping.allowedOrigins("*")
        } else {
            mapping.allowedOrigins(*origins.toTypedArray())
        }
    }
}
