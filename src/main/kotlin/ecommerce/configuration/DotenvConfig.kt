package ecommerce.configuration

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.context.annotation.Configuration

@Configuration
class DotenvConfig {
    companion object {
        init {
            val dotenv = Dotenv.configure().ignoreIfMissing().load()
            dotenv.entries().forEach { System.setProperty(it.key, it.value) }
        }
    }
}
