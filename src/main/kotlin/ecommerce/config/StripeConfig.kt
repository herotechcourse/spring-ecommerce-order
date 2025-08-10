package ecommerce.config

import com.stripe.Stripe
import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(StripeProperties::class)
class StripeConfig(private val stripeProperties: StripeProperties) {
    @PostConstruct
    fun init() {
        Stripe.apiKey = stripeProperties.secretKey
    }
}
