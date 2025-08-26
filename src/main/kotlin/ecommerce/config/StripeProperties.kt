package ecommerce.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "stripe")
data class StripeProperties(val secretKey: String, val url: String)
