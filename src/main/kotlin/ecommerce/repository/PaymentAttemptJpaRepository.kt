package ecommerce.repository

import ecommerce.entity.PaymentAttempt
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentAttemptJpaRepository : JpaRepository<PaymentAttempt, Long>
