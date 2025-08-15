package ecommerce.services.stripe

import ecommerce.exception.PaymentFailedException
import ecommerce.infrastructure.StripeClient
import ecommerce.model.PaymentRequest
import ecommerce.model.StripeResponse
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap

@Service
class StripeService(
    private val stripeClient: StripeClient,
) {
    fun createPaymentIntent(
        request: PaymentRequest,
        isConfirmed: Boolean = true,
    ): StripeResponse {
        val form =
            LinkedMultiValueMap<String, String>().apply {
                add("amount", request.amountInSmallestUnit.toString())
                add("currency", request.currency)
                add("payment_method", request.paymentMethod)
                add("confirm", isConfirmed.toString())
                add("automatic_payment_methods[enabled]", "true")
                add("automatic_payment_methods[allow_redirects]", "never")
            }

        return try {
            stripeClient.createPaymentIntent(form)
        } catch (e: Exception) {
            throw PaymentFailedException("Stripe payment initialization failed: ${e.message}", e)
        }
    }
}
