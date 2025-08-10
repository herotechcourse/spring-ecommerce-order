package ecommerce.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StripeClientTest(
    @Autowired val client: StripeClient,
) {
    @Test
    fun `payment status success with valid payment method`() {
        val amount = 100
        val currency = "USD"
        val paymentMethod = "pm_card_visa"
        val actual =
            client.createCheckoutSession(
                PaymentRequest(
                    amount = amount,
                    currency = currency,
                    paymentMethod = paymentMethod,
                ),
            )

        val expected = "succeeded"
        assertThat(actual).isNotNull()
        assertThat(actual?.amount).isEqualTo(amount)
        assertThat(actual?.`object`).isEqualTo("payment_intent")
        assertThat(actual?.status).isEqualTo(expected)
    }

    @Test
    fun `payment status failed with invalid payment method, card declined with Generic decline`() {
        val amount = 100
        val currency = "USD"
        val paymentMethod = "pm_card_visa_chargeDeclined"
        assertThrows<IllegalArgumentException> {
            client.createCheckoutSession(
                PaymentRequest(
                    amount = amount,
                    currency = currency,
                    paymentMethod = paymentMethod,
                ),
            )
        }
    }

    @Test
    fun `payment status failed with invalid payment method, card declined with Insufficient funds decline`() {
        val amount = 100
        val currency = "USD"
        val paymentMethod = "pm_card_visa_chargeDeclinedInsufficientFunds"
        assertThrows<IllegalArgumentException> {
            client.createCheckoutSession(
                PaymentRequest(
                    amount = amount,
                    currency = currency,
                    paymentMethod = paymentMethod,
                ),
            )
        }
    }

    @Test
    fun `payment status failed with invalid payment method, card declined with Lost card decline`() {
        val amount = 100
        val currency = "USD"
        val paymentMethod = "pm_card_visa_chargeDeclinedLostCard"
        assertThrows<IllegalArgumentException> {
            client.createCheckoutSession(
                PaymentRequest(
                    amount = amount,
                    currency = currency,
                    paymentMethod = paymentMethod,
                ),
            )
        }
    }

    @Test
    fun `payment status failed with invalid payment method, expired card declined`() {
        val amount = 100
        val currency = "USD"
        val paymentMethod = "pm_card_chargeDeclinedExpiredCard"
        assertThrows<IllegalArgumentException> {
            client.createCheckoutSession(
                PaymentRequest(
                    amount = amount,
                    currency = currency,
                    paymentMethod = paymentMethod,
                ),
            )
        }
    }

    @Test
    fun `payment status failed with invalid payment method, processing error declined`() {
        val amount = 100
        val currency = "USD"
        val paymentMethod = "pm_card_chargeDeclinedProcessingError"
        assertThrows<IllegalArgumentException> {
            client.createCheckoutSession(
                PaymentRequest(
                    amount = amount,
                    currency = currency,
                    paymentMethod = paymentMethod,
                ),
            )
        }
    }
}
