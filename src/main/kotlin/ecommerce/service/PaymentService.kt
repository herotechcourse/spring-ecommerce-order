package ecommerce.service

import ecommerce.client.StripeClient
import ecommerce.dto.OrderRequest
import ecommerce.dto.PaymentResponse
import ecommerce.enum.PaymentStatus
import ecommerce.exception.StripePaymentException
import ecommerce.model.Payment
import ecommerce.repository.PaymentJpaRepository
import ecommerce.repository.getByPaymentIntentIdOrThrow
import org.hibernate.service.spi.ServiceException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Transactional
@Service
class PaymentService(
    private val stripeClient: StripeClient,
    private val paymentJpaRepository: PaymentJpaRepository,
) {
    fun createPaymentIntent(
        request: OrderRequest,
        amount: BigDecimal,
    ): PaymentResponse {
        try {
            val amountInCents =
                amount
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP)
                    .toLong()
            val paymentResponse =
                stripeClient.createCheckoutSession(request, amountInCents)
                    ?: throw ServiceException("Payment Failed")

            val payment =
                Payment(
                    amount = amount,
                    currency = request.currency,
                    paymentMethod = request.paymentMethod,
                    paymentIntentId = paymentResponse.id,
                )
            paymentJpaRepository.save(payment)
            return paymentResponse
        } catch (e: StripePaymentException) {
            val paymentResponse =
                PaymentResponse(
                    id = "Not Available",
                    amount = amount.toDouble(),
                    status = PaymentStatus.FAILED.name,
                    errorMessage = e.message,
                )
            val payment =
                Payment(
                    amount = amount,
                    currency = request.currency,
                    paymentMethod = request.paymentMethod,
                    paymentIntentId = "Payment Intent Failed",
                    status = PaymentStatus.FAILED,
                )
            paymentJpaRepository.save(payment)
            return paymentResponse
        }
    }

    fun updatePaymentStatus(
        paymentIntentId: String,
        status: PaymentStatus,
    ) {
        val payment = paymentJpaRepository.getByPaymentIntentIdOrThrow(paymentIntentId)
        payment.status = status
        paymentJpaRepository.save(payment)
    }
}
