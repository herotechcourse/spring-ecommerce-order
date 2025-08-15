package ecommerce.config.security

import io.github.cdimascio.dotenv.dotenv
import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource

class DotenvEnvironmentPostProcessor : EnvironmentPostProcessor {
    override fun postProcessEnvironment(
        env: ConfigurableEnvironment,
        app: SpringApplication,
    ) {
        val dotenv =
            dotenv {
                directory = "."
                ignoreIfMissing = true
            }

        val props = dotenv.entries().associate { it.key to it.value }
        env.propertySources.addFirst(MapPropertySource("dotenv", props))
    }
}
