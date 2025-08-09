package ecommerce

import ecommerce.config.StripeProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(StripeProperties::class)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
