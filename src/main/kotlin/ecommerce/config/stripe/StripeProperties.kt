package ecommerce.config.stripe

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("stripe")
class StripeProperties(
    val secretKey: String,
)
