package ecommerce.model

import ecommerce.enums.OrderStatus
import ecommerce.enums.PaymentOption
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MemberOrderTest {
    @Test
    fun incrementAttempt() {
        val memberOrder =
            MemberOrder(
                listOf(),
                0L,
                "user@mail.com",
                "stipeId",
                PaymentOption.STRIPE,
                0.0,
                OrderStatus.COMPLETED,
            )
        memberOrder.incrementAttempt()
        assertThat(memberOrder.attempt).isEqualTo(2)
    }
}
