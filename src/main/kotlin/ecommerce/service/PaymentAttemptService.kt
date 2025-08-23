package ecommerce.service

import ecommerce.entity.PaymentAttempt
import ecommerce.entity.enumerated.PaymentAttemptStatus
import ecommerce.repository.PaymentAttemptJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentAttemptService(
    private val paymentAttemptRepo: PaymentAttemptJpaRepository,
) {
    @Transactional
    fun updateStatus(
        paymentAttemptId: Long,
        status: PaymentAttemptStatus,
    ) {
        val paymentAttempt = paymentAttemptRepo.findById(paymentAttemptId).orElseThrow()
        paymentAttempt.status = status
    }

    @Transactional(readOnly = true)
    fun findById(paymentAttemptId: Long): PaymentAttempt {
        return paymentAttemptRepo.findByIdOrNull(paymentAttemptId)
            ?: throw NoSuchElementException("Payment attempt with id $paymentAttemptId not found")
    }
}
