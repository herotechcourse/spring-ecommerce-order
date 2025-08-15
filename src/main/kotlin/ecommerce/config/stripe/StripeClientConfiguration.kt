package ecommerce.config.stripe

import ecommerce.infrastructure.StripeClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class StripeClientConfiguration {
    @Bean
    fun stripeClient(stripeProperties: StripeProperties): StripeClient {
        val restClient =
            RestClient.builder()
                .baseUrl("https://api.stripe.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer ${stripeProperties.secretKey}")
                .build()

        val adapter = RestClientAdapter.create(restClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter).build()

        return factory.createClient(StripeClient::class.java)
    }
}
