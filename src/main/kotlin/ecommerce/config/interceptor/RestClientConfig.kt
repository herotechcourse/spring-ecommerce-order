package ecommerce.config.interceptor

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {
    @Bean
    fun stripeRestClient(): RestClient {
        val factory =
            SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(CONNECTION_TIMEOUT)
                setReadTimeout(READ_TIMEOUT)
            }

        return RestClient.builder()
            .requestFactory(factory)
            .build()
    }

    companion object {
        private const val CONNECTION_TIMEOUT = 5_000
        private const val READ_TIMEOUT = 60_000
    }
}
