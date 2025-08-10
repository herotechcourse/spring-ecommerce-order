package ecommerce.service

import ecommerce.client.StripeClient
import ecommerce.dto.PaymentRequest
import ecommerce.exception.BadRequestException
import ecommerce.exception.ExternalServiceException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class PaymentService(
    private val stripeClient: StripeClient,
) {
    fun createPaymentIntent(req: PaymentRequest): String? {
        try {
            val response = stripeClient.createCheckoutSession(req)
            return response
        } catch (e: BadRequestException) {
            throw e
        } catch (e: ExternalServiceException) {
            throw e
        } catch (e: Exception) {
            // retry??
            throw RuntimeException(e) // this should roll back the transaction
        }
    }
}
