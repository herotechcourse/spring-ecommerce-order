package ecommerce.client

import ecommerce.dto.PaymentRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class StripeClientTest {

    @Autowired
    lateinit var stripeClient: StripeClient

    @Test
    fun `createCheckoutSession returns session info`() {
        val paymentRequest = PaymentRequest(
            amount = 1000,
            currency = "usd",
            paymentMethod = "pm_card_visa"
        )

        val result = stripeClient.createCheckoutSession(paymentRequest)

        println("Stripe Response: $result")
        assertThat(result).isNotBlank()
    }
}