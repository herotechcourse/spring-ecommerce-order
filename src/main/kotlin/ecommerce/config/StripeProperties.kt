package ecommerce.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "stripe")
data class StripeProperties(
    val secretKey: String,
    val connectTimeoutMs: Long = 3000,
    val readTimeoutMs: Long = 10000,
)

@Configuration
@EnableConfigurationProperties(StripeProperties::class)
class StripeConfig
