package ecommerce.exception

import ecommerce.dto.errors.StripeErrorMessage
import ecommerce.dto.errors.StripeErrorResponse

class StripeClientException(val stripeErrorResponse: StripeErrorResponse) :
    RuntimeException(stripeErrorResponse.error.message) {
    companion object {
        fun from(message: String): StripeClientException {
            return StripeClientException(
                StripeErrorResponse(
                    StripeErrorMessage(
                        code = "stripe_payment_failed",
                        message = message,
                    ),
                ),
            )
        }
    }
}
