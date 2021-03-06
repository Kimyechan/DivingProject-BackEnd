package com.diving.pungdong.repo;

import com.diving.pungdong.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepo extends JpaRepository<Payment, Long> {
}
