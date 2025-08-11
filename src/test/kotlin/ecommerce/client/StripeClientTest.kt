package ecommerce.client

import ecommerce.exception.PaymentClientException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class StripeClientTest {

    @Autowired
    lateinit var stripeClient: StripeClient

    @Test
    fun `createPaymentIntent with invalid key maps to PaymentClientException`() {
        assertThatThrownBy {
            stripeClient.createPaymentIntent(amount = 1000, currency = "usd")
        }.isInstanceOf(PaymentClientException::class.java)
    }
}