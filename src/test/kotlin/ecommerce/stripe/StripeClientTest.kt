package ecommerce.stripe

import ecommerce.client.StripeClient
import ecommerce.dto.OrderRequest
import ecommerce.enum.Currency
import ecommerce.exception.StripePaymentException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    properties = [
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=none",
    ],
)
@ActiveProfiles("test")
class StripeClientTest {
    @Autowired
    private lateinit var stripeClient: StripeClient

    @Test
    fun `should create a payment intent with the right amount`() {
        val actual =
            stripeClient.createCheckoutSession(
                OrderRequest(
                    Currency.EUR,
                    "pm_card_visa",
                ),
                amount = 1000,
            )
        assertThat(actual).isNotNull
        assertThat(actual?.amount).isEqualTo(10.0)
    }

    @Test
    fun test_declined_card() {
        assertThatThrownBy {
            stripeClient.createCheckoutSession(
                OrderRequest(
                    currency = Currency.EUR,
                    paymentMethod = "pm_card_chargeDeclined",
                ),
                amount = 1000,
            )
        }
            .isInstanceOf(StripePaymentException::class.java)
            .hasMessageContaining("Your card was declined")
    }

    @Test
    fun test_DeclinedInsufficientFunds() {
        assertThatThrownBy {
            stripeClient.createCheckoutSession(
                OrderRequest(
                    currency = Currency.EUR,
                    paymentMethod = "pm_card_visa_chargeDeclinedInsufficientFunds",
                ),
                amount = 1000,
            )
        }
            .isInstanceOf(StripePaymentException::class.java)
            .hasMessageContaining("insufficient funds")
    }
}
