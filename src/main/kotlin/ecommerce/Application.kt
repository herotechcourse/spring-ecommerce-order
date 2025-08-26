package ecommerce

import ecommerce.stripe.StripeProperties
import io.github.cdimascio.dotenv.Dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(StripeProperties::class)
@SpringBootApplication
class Application

fun main(args: Array<String>) {
    val dotenv = Dotenv.load()

    System.setProperty("STRIPE_SECRET_KEY", dotenv["STRIPE_SECRET_KEY"])

    runApplication<Application>(*args)
}
