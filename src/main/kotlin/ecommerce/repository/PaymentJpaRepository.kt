package ecommerce.repository

import ecommerce.exception.ElementNotFoundException
import ecommerce.model.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentJpaRepository : JpaRepository<Payment, Long> {
    fun findByPaymentIntentId(paymentIntentId: String): Payment?
}

fun PaymentJpaRepository.getByPaymentIntentIdOrThrow(paymentIntentId: String): Payment =
    findByPaymentIntentId(paymentIntentId)
        ?: throw ElementNotFoundException("No payment found with payment intent ID: $paymentIntentId")
