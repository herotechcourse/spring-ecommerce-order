package ecommerce

import ecommerce.config.security.JwtProperties
import ecommerce.config.stripe.StripeProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties::class, StripeProperties::class)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
