package ecommerce.stripe

import ecommerce.dto.stripe.PaymentRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = ["spring.sql.init.mode=never"])
@SpringBootTest
class StripeClientTest {
    @Autowired
    private lateinit var stripeClient: StripeClient

    @Test
    fun testForSuccessCase() {
        val actual =
            stripeClient.createCheckoutSession(
                PaymentRequest(
                    amount = 1000,
                    currency = "EUR",
                    paymentMethod = "pm_card_visa",
                ),
            )
        assertThat(actual.id).isNotNull()
        assertThat(actual.status).isEqualTo("succeeded")
        assertThat(actual.amount).isEqualTo(1000)
    }

    @Test
    fun testDeclinedPaymentForInsufficientFunds() {
        val failed =
            stripeClient.createCheckoutSession(
                PaymentRequest(
                    amount = 1000,
                    currency = "EUR",
                    paymentMethod = "pm_card_visa_chargeDeclinedInsufficientFunds",
                ),
            )
        assertThat(failed.status).isEqualTo("FAILED")
        assertThat(failed.errorMessage).contains("insufficient balance")
    }

    @Test
    fun testDeclinedPaymentForStolenCard() {
        val failed =
            stripeClient.createCheckoutSession(
                PaymentRequest(
                    amount = 1000,
                    currency = "EUR",
                    paymentMethod = "pm_card_visa_chargeDeclinedStolenCard",
                ),
            )
        assertThat(failed.status).isEqualTo("FAILED")
        assertThat(failed.errorMessage).contains("stolen card")
    }
}
