package ecommerce.services.payment

import ecommerce.dto.PaymentDTO
import ecommerce.entities.OrderEntity
import ecommerce.entities.PaymentEntity
import ecommerce.mappers.toDTO
import ecommerce.model.StripeResponse
import ecommerce.repositories.PaymentRepository
import org.springframework.stereotype.Service

@Service
class PaymentService(private val paymentRepository: PaymentRepository) {
    fun createPayment(
        order: OrderEntity,
        stripeResponse: StripeResponse,
    ): PaymentDTO {
        val payment =
            PaymentEntity(
                stripePaymentIntentId = stripeResponse.id!!,
                amount = stripeResponse.amount ?: 0,
                currency = stripeResponse.currency ?: "EUR",
                status = PaymentEntity.PaymentStatus.fromValue(stripeResponse.status!!),
                failureCode = stripeResponse.failureCode,
                failureMessage = stripeResponse.failureMessage,
                order = order,
            )
        return paymentRepository.save(payment).toDTO()
    }
}
